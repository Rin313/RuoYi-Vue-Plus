package org.dromara.system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.dromara.common.core.BizException;
import org.dromara.system.domain.Chapter;
import org.dromara.system.domain.ChapterAnalysisResult;
import org.dromara.system.domain.ChapterAnalysisResult.CharacterUpdate;
import org.dromara.system.domain.Novel.Character;
import org.dromara.system.domain.ChatMessage;
import org.dromara.system.domain.GenerationOptions;
import org.dromara.system.domain.Novel;
import org.dromara.system.domain.Story;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryEngineService {
    private final NovelService novelService;
    private final StoryService storyService;
    private final ChapterService chapterService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    // 定义结构化输出 Schema 
    private static final Map<String, Object> CHAPTER_ANALYSIS_SCHEMA = Map.of(
            "type", "OBJECT",
            "properties", Map.of(
                    "newSummary", Map.of("type", "STRING", "description", "Concise summary of the story so far, merging previous summary with new events."),
                    "characterUpdates", Map.of(
                            "type", "ARRAY",
                            "items", Map.of(
                                    "type", "OBJECT",
                                    "properties", Map.of(
                                            "name", Map.of("type", "STRING"),
                                            "newAttributes", Map.of("type", "STRING", "description", "Updated status based on recent events.")
                                    )
                            )
                    )
            )
    );

    /**
     * 核心功能 1: 交互式故事生成
     */
    public void continueStory(Long chapterId,String userMessage,Consumer<String> onChunk, Runnable onComplete) {
        Chapter chapter=chapterService.getById(chapterId);
        Story story=storyService.getById(chapter.getStoryId());
        Novel novel=novelService.getById(story.getNovelId());
        Character player=getPlayer(story.getCharacters());

        // 构建 System Instruction 
        String systemPrompt = buildSystemPrompt(novel,story,chapter,player);

        // 配置参数 
        GenerationOptions options = GenerationOptions.builder()
                .temperature(0.9)
                .topP(0.95)
                .maxOutputTokens(512)
                .build();

        StringBuilder fullResponseAccumulator = new StringBuilder();
        // 调用流式生成
        geminiService.generateStream(
                systemPrompt,
                chapter.getChatHistory(),
                userMessage,
                options,
                (chunk) -> {
                    fullResponseAccumulator.append(chunk);
                    onChunk.accept(chunk);
                },
                () -> {
                    chapterService.addMessages(chapter, new ChatMessage("user", userMessage),new ChatMessage("model", fullResponseAccumulator.toString()));
                    onComplete.run();
                },
                throwable -> log.error("Story generation failed", throwable)
        );
    }

    /**
     * 核心功能 2: 章节结算
     * @param context 基础上下文
     * @param characters 当前角色列表
     * @param pureStoryText 这一章纯粹的 AI 生成文本 (清洗掉用户的指令，只保留故事内容)
     */
    public void concludeChapter(Long chapterId) {
        Chapter chapter=chapterService.getById(chapterId);
        Story story=storyService.getById(chapter.getStoryId());
        List<ChatMessage> history=chapter.getChatHistory();
        StringBuilder pureStoryText = new StringBuilder();
        for(ChatMessage msg:history){
            if(!msg.getRole().equals("user"))
                pureStoryText.append(msg.getContent());
        }
        // 这里不需要把世界观全扔进去，只需要前文摘要和新内容即可
        String analysisPrompt = """
                You are a Database Updater for an interactive novel system.
                
                ### TASK
                1. **Update Summary**: Incorporate the [New Chapter Content] into the [Previous Summary]. Keep the total summary coherent and under 800 words. Focus on major plot points and character arcs.
                2. **Update Characters**: specificly analyze the physical/mental state, inventory, and location of ALL characters based on the new text.
                
                ### INPUT DATA
                <previous_summary>
                %s
                </previous_summary>
                
                <character_current_states>
                %s
                </character_current_states>
                
                <new_chapter_content>
                %s
                </new_chapter_content>
                """.formatted(
                        story.getPreviousSummary(),
                        formatCharacterStates(story.getCharacters()),
                        pureStoryText 
                );
        try {
            String jsonResult = geminiService.generateStructuredData(
                    analysisPrompt,
                    "Perform the chapter analysis and return valid JSON.",
                    CHAPTER_ANALYSIS_SCHEMA 
            );
            ChapterAnalysisResult result = objectMapper.readValue(jsonResult, ChapterAnalysisResult.class);
            
            story.setPreviousSummary(result.getNewSummary());
            

            if (result.getCharacterUpdates() != null) {
                for (CharacterUpdate update : result.getCharacterUpdates()) {
                    for (Character character : story.getCharacters()) {
                        if (character.getName().equalsIgnoreCase(update.getName())) {
                            character.setCurrentAttributes(update.getNewAttributes());
                            break;
                        }
                    }
                }
            }
            storyService.updateById(story);
        } catch (Exception e) {
            log.error("Failed to conclude chapter {}", chapterId, e);
            throw new BizException("Failed to conclude chapter");
        }
    }

    /**
     * 构建缓存友好的 System Prompt
     */
    private String buildSystemPrompt(Novel novel,Story story,Chapter chapter,Character player) {

        StringBuilder sb = new StringBuilder();
        // --- SECTION 1: IDENTITY & RULES (Static) ---
        sb.append("You are an expert AI Game Master (GM) and Novelist.\n");
        sb.append("Your Logic: 1. Analyze User Input -> 2. Determine if it's an Action or a Directive -> 3. Simulate World/Characters -> 4. Narrate the outcome.\n\n");

        // --- SECTION 2: WORLD & STORY (Static - Heavy Cache Hit) ---
        sb.append("<world_setting>\n").append(novel.getWorldSetting()).append("\n</world_setting>\n\n");
        sb.append("<main_storyline>\n").append(novel.getMainStoryline()).append("\n</main_storyline>\n\n");

        // --- SECTION 3: CHARACTERS (Semi-Static) ---
        List<Character> characters=story.getCharacters();
        sb.append("<character_roster>\n");
        for(Character character:characters){
            sb.append(String.format("- Name: %s\n  Bio: %s\n", character.getName(), character.getBaseDescription()));
        }
        sb.append("</character_roster>\n\n");
        // --- SECTION 4: STYLE GUIDELINES (Static) ---
        sb.append("<writing_rules>\n");
        sb.append("### CORE WRITING STANDARDS\n");
        sb.append("1. **Show, Don't Tell**: Use sensory details (sight, sound, smell) to imply emotion.\n");
        sb.append("2. **Dynamic Structure**: Mix [Action/Environment] -> [Dialogue] -> [Reaction]. Break long dialogue with action beats.\n");
        sb.append("3. **Dialogue Reality**: Keep speech colloquial and fragmented. High information density.\n");
        sb.append("4. **Adaptive Pacing**: Response length must match the context. \n");
        sb.append("   - User Action -> Immediate Consequence.\n");
        sb.append("   - User Directive (Plot change) -> Scene setting + Hook.\n");
        // 如果中有特定的风格要求（比如：克苏鲁风格、古龙风格），可以追加在后面
        if (novel.getWritingStyle() != null) {
             sb.append("\n### SPECIFIC TONE SETTINGS\n").append(novel.getWritingStyle()).append("\n");
        }
        sb.append("</writing_rules>\n\n");

        // --- SECTION 5: DYNAMIC CONTEXT (Changes per Chapter) ---
        sb.append("--- CURRENT CHAPTER CONTEXT ---\n\n");

        if (story.getPreviousSummary() != null && !story.getPreviousSummary().isEmpty()) {
            sb.append("<previous_story_summary>\n").append(story.getPreviousSummary()).append("\n</previous_story_summary>\n\n");
        }

        // 动态状态放在最后
        sb.append("<current_character_status>\n");
        sb.append(formatCharacterStates(characters));
        sb.append("</current_character_status>\n\n");

        // 明确当前扮演者，解决“导演”vs“演员”问题
        sb.append("<player_role>\n");
        sb.append("User is currently controlling: **").append(player.getName()).append("**.\n");
        sb.append("NOTE: The user acts as both the Actor (").append(player.getName()).append(") and the Director.\n");
        sb.append("- If input is an ACTION (e.g., 'I draw my sword'): Narrate the result from ").append(player.getName()).append("'s perspective.\n");
        sb.append("- If input is a DIRECTIVE (e.g., 'Skip to morning', 'A dragon appears'): Execute the command and narrate the new scene.\n");
        sb.append("</player_role>\n");

        sb.append("--- OUTPUT FORMAT REQUIREMENTS ---\n");
        sb.append("1. OUTPUT ONLY THE STORY CONTENT. Do not include any explanations, mental notes, or labels.\n");
        sb.append("2. Keep the response pacing natural. If the user input is short, the response should be proportional, not forcing a full chapter structure every turn.\n");
        return sb.toString();
    }
    private String formatCharacterStates(List<Character> characters) {
        return characters.stream()
                .map(character -> String.format("- %s: %s", character.getName(), character.getCurrentAttributes()))
                .collect(Collectors.joining("\n"));
    }
    
    private Character getPlayer(List<Character> characters) {
        for(Character character:characters){
            if(character.getIsPlayer())
                return character;
        }
        return null;
    }
}