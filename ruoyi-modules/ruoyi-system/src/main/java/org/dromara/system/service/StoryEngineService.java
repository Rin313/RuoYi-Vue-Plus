package org.dromara.system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.dromara.system.domain.ChapterAnalysisResult;
import org.dromara.system.domain.CharacterProfile;
import org.dromara.system.domain.ChatMessage;
import org.dromara.system.domain.GenerationOptions;
import org.dromara.system.domain.NovelContext;
import org.dromara.system.domain.PlayRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryEngineService {

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
    public void continueStory(PlayRequest request, Consumer<String> onChunk, Runnable onComplete) {
        
        // 1. 构建 System Instruction 
        String systemPrompt = buildSystemPrompt(request);

        // 2. 处理历史记录
        List<ChatMessage> history = request.getCurrentChapterHistory();

        // 3. 构建用户消息
        String userMessage = formatUserEntry(request);

        // 4. 配置参数 
        GenerationOptions options = GenerationOptions.builder()
                .temperature(0.9)
                .topP(0.95)
                .maxOutputTokens(2048)
                .build();

        // 5. 调用流式生成
        geminiService.generateStream(
                systemPrompt,
                history,
                userMessage,
                options,
                onChunk,
                onComplete,
                throwable -> log.error("Story generation failed", throwable)
        );
    }

    /**
     * 核心功能 2: 章节结算
     * @param context 基础上下文
     * @param characters 当前角色列表
     * @param pureStoryText 这一章纯粹的 AI 生成文本 (清洗掉用户的指令，只保留故事内容)
     */
    public ChapterAnalysisResult concludeChapter(NovelContext context, 
                                                 List<CharacterProfile> characters, 
                                                 String pureStoryText) {
        // 构建提示词
        // 注意：这里不需要把世界观全扔进去，只需要前文摘要和新内容即可
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
                        context.getPreviousSummary(),
                        formatCharacterStates(characters),
                        pureStoryText 
                );

        String jsonResult = geminiService.generateStructuredData(
                analysisPrompt, 
                "Perform the chapter analysis and return JSON.", 
                CHAPTER_ANALYSIS_SCHEMA
        );

        try {
            return objectMapper.readValue(jsonResult, ChapterAnalysisResult.class);
        } catch (Exception e) {
            log.error("Analysis Parsing Error. Raw JSON: {}", jsonResult);
            throw new RuntimeException("Failed to analyze chapter", e);
        }
    }

    // ================== 核心 Prompt 构建逻辑 ==================

    /**
     * 构建缓存友好的 System Prompt
     * 策略：最重且不变的内容放在最前面 -> 变化的内容放在最后
     */
    private String buildSystemPrompt(PlayRequest request) {
        NovelContext ctx = request.getContext();
        StringBuilder sb = new StringBuilder();

        // --- SECTION 1: IDENTITY & RULES (Static) ---
        sb.append("You are an expert AI Game Master (GM) and Novelist.\n");
        sb.append("Your Logic: 1. Analyze User Input -> 2. Determine if it's an Action or a Directive -> 3. Simulate World/Characters -> 4. Narrate the outcome.\n\n");

        // --- SECTION 2: WORLD & STORY (Static - Heavy Cache Hit) ---
        sb.append("<world_setting>\n").append(ctx.getWorldSetting()).append("\n</world_setting>\n\n");
        sb.append("<main_storyline>\n").append(ctx.getMainStoryline()).append("\n</main_storyline>\n\n");

        // --- SECTION 3: CHARACTERS (Semi-Static) ---
        sb.append("<character_roster>\n");
        for (CharacterProfile c : request.getCharacters()) {
            sb.append(String.format("- Name: %s\n  Bio: %s\n", c.getName(), c.getBaseDescription()));
        }
        sb.append("</character_roster>\n\n");

        // --- SECTION 4: STYLE GUIDELINES (Static) ---
        sb.append("<writing_rules>\n");
        sb.append(ctx.getWritingStyle()).append("\n");
        sb.append("</writing_rules>\n\n");

        // --- SECTION 5: DYNAMIC CONTEXT (Changes per Chapter) ---
        sb.append("--- CURRENT CHAPTER CONTEXT ---\n\n");

        if (ctx.getPreviousSummary() != null && !ctx.getPreviousSummary().isEmpty()) {
            sb.append("<previous_story_summary>\n").append(ctx.getPreviousSummary()).append("\n</previous_story_summary>\n\n");
        }

        // 动态状态放在最后，确保模型使用最新的状态生成
        sb.append("<current_character_status>\n");
        sb.append(formatCharacterStates(request.getCharacters()));
        sb.append("</current_character_status>\n\n");

        // 明确当前扮演者，解决“导演”vs“演员”问题
        CharacterProfile player = getPlayerProfile(request);
        if (player != null) {
            sb.append("<player_role>\n");
            sb.append("User is currently controlling: **").append(player.getName()).append("**.\n");
            sb.append("NOTE: The user acts as both the Actor (").append(player.getName()).append(") and the Director.\n");
            sb.append("- If input is an ACTION (e.g., 'I draw my sword'): Narrate the result from ").append(player.getName()).append("'s perspective.\n");
            sb.append("- If input is a DIRECTIVE (e.g., 'Skip to morning', 'A dragon appears'): Execute the command and narrate the new scene.\n");
            sb.append("</player_role>\n");
        }
        sb.append("--- OUTPUT FORMAT REQUIREMENTS ---\n");
        sb.append("1. OUTPUT ONLY THE STORY CONTENT. Do not include any explanations, mental notes, or labels.\n");
        sb.append("2. DO NOT use Markdown headers (like # Chapter 1, ## Scene, ### Action). Just plain paragraphs.\n");
        sb.append("3. DO NOT output structure labels like '[Environment]', '[Dialogue]'.\n");
        sb.append("4. Keep the response pacing natural. If the user input is short, the response should be proportional, not forcing a full chapter structure every turn.\n");
        return sb.toString();
    }

    /**
     * 包装用户输入
     */
    private String formatUserEntry(PlayRequest request) {
        CharacterProfile player = getPlayerProfile(request);
        String playerName = (player != null) ? player.getName() : "Protagonist";

        // 提供上下文结构，让 System Prompt 去判断意图
        return String.format("""
                <user_input>
                <context_role>%s</context_role>
                <content>%s</content>
                </user_input>
                """, playerName, request.getUserAction());
    }

    private String formatCharacterStates(List<CharacterProfile> characters) {
        return characters.stream()
                .map(c -> String.format("- %s: %s", c.getName(), c.getCurrentAttributes()))
                .collect(Collectors.joining("\n"));
    }
    
    private CharacterProfile getPlayerProfile(PlayRequest request) {
        return request.getCharacters().stream()
                .filter(CharacterProfile::isPlayer)
                .findFirst()
                .orElse(null);
    }
}