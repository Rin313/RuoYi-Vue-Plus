package org.dromara.system.service;

import com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.*;
import lombok.RequiredArgsConstructor;

import org.dromara.common.core.config.GeminiProperties;
import org.dromara.system.domain.ChatMessage;
import org.dromara.system.domain.GenerationOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class GeminiService {
    private final Client client;
    private final GeminiProperties properties;
    private static final ImmutableList<SafetySetting> DEFAULT_SAFETY_SETTINGS = ImmutableList.of();
    private static final ThinkingConfig NO_THINKING_CONFIG = ThinkingConfig.builder().thinkingBudget(0).build();
    // 用于摘要生成、角色属性更新等
    public String generateSync(String systemPrompt, String userMessage, GenerationOptions options) {
        List<Content> contents = buildChatContents(null, userMessage);
        GenerateContentConfig config = createConfig(systemPrompt, options);

        try {
            GenerateContentResponse response = client.models.generateContent(
                    properties.getModel(), 
                    contents, 
                    config
            );
            return response.text();
        } catch (Exception e) {
            throw new RuntimeException("AI服务调用失败", e);
        }
    }
    /**
     * JSON格式数据生成接口
     * 适用于：章节总结、角色属性更新
     */
    public String generateStructuredData(String systemPrompt, 
                                         String userMessage, 
                                         Map<String, Object> jsonSchema) {
        // 1. 构建 Prompt (通常分析任务不需要太长的历史对话，主要是基于前文摘要+当前章节内容)
        List<Content> contents = buildChatContents(null, userMessage);
        // 2. 构建配置，开启 JSON 模式
        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(Content.fromParts(Part.fromText(systemPrompt)))
                .responseMimeType("application/json")
                .responseJsonSchema(jsonSchema)
                .candidateCount(1)
                .temperature(0.2f) // 分析任务降低随机性
                .build();
        try {
            GenerateContentResponse response = client.models.generateContent(
                    properties.getModel(),
                    contents,
                    config
            );
            return response.text();
        } catch (Exception e) {
            throw new RuntimeException("AI结构化分析失败", e);
        }
    }
    /**
     * 流式生成接口
     * 适用于：普通对话、角色扮演、交互式故事
     *
     * @param systemPrompt 系统指令/人设 (可为 null)
     * @param history      历史对话记录 (可为 null 或 空)
     * @param userMessage  当前用户输入
     * @param options      动态参数配置 (可为 null，将使用默认值)
     * @param onChunk      接收数据块的回调
     * @param onComplete   完成时的回调
     * @param onError      异常回调
     */
    public void generateStream(String systemPrompt, 
                               List<ChatMessage> history, 
                               String userMessage,
                               GenerationOptions options,
                               Consumer<String> onChunk, 
                               Runnable onComplete, 
                               Consumer<Throwable> onError) {
        
        // 构建完整的上下文内容 (History + Current Message)
        List<Content> contents = buildChatContents(history, userMessage);
        
        // 构建配置 (合并 系统提示词 + 默认配置 + 前端动态参数)
        GenerateContentConfig config = createConfig(systemPrompt, options);

        try (ResponseStream<GenerateContentResponse> stream = 
                client.models.generateContentStream(properties.getModel(), contents, config)) {
            
            // 处理流
            processStreamResponse(stream, onChunk);
            
            if (onComplete != null) onComplete.run();
            
        } catch (Exception e) {
            if (onError != null) onError.accept(e);
        }
    }

    /**
     * 核心流处理逻辑
     */
    private void processStreamResponse(ResponseStream<GenerateContentResponse> stream, Consumer<String> onChunk) {
        for (GenerateContentResponse response : stream) {
            String text = response.text();
            if (text != null && !text.isEmpty()) {
                onChunk.accept(text);
            }
        }
    }

    /**
     * 构建配置：优先使用 options 中的参数，不存在则使用 properties 中的默认值
     */
    private GenerateContentConfig createConfig(String systemPrompt, GenerationOptions options) {
        Content systemInstruction = systemPrompt != null && !systemPrompt.isBlank()
                ? Content.fromParts(Part.fromText(systemPrompt))
                : null;

        // 获取基础配置值 (优先取 options，为 null 则取 properties)
        float temperature = (options != null && options.getTemperature() != null)
                ? options.getTemperature().floatValue()
                : properties.getTemperature();

        float topP = (options != null && options.getTopP() != null)
                ? options.getTopP().floatValue()
                : properties.getTopP();

        int maxTokens = (options != null && options.getMaxOutputTokens() != null)
                ? options.getMaxOutputTokens()
                : properties.getMaxOutputTokens();
        
        // 思考模式配置
        ThinkingConfig thinkingConfig = NO_THINKING_CONFIG;
        if (options != null && Boolean.TRUE.equals(options.getEnableThinking())) {
             // 这里可以根据需求开启 thinking，目前保持默认关闭
             // thinkingConfig = ThinkingConfig.builder().thinkingBudget(1024).build(); 
        }

        return GenerateContentConfig.builder()
                .systemInstruction(systemInstruction)
                .candidateCount(1)
                .thinkingConfig(thinkingConfig)
                .maxOutputTokens(maxTokens)
                .temperature(temperature)
                .topP(topP)
                .safetySettings(DEFAULT_SAFETY_SETTINGS)
                .build();
    }
    /**
     * 构建对话内容列表
     */
    private List<Content> buildChatContents(List<ChatMessage> history, String userMessage) {
        ImmutableList.Builder<Content> contentsBuilder = ImmutableList.builder();
        if (history != null) {
            for (ChatMessage msg : history) {
                if (msg.getContent() != null) {
                    contentsBuilder.add(
                            Content.builder()
                                    .role(msg.getRole())
                                    .parts(Part.fromText(msg.getContent()))
                                    .build()
                    );
                }
            }
        }
        // 添加当前用户消息
        contentsBuilder.add(
                Content.builder()
                        .role("user")
                        .parts(Part.fromText(userMessage))
                        .build()
        );
        
        return contentsBuilder.build();
    }
}


/*
        // 允许角色冲突、傲娇、辱骂等剧情
        SafetySetting.builder()
            .category(HarmCategory.Known.HARM_CATEGORY_HARASSMENT)
            .threshold(HarmBlockThreshold.Known.BLOCK_NONE)
            .build(),
        // 允许战斗、战争、虚构的危险行为描写
        SafetySetting.builder()
            .category(HarmCategory.Known.HARM_CATEGORY_DANGEROUS_CONTENT)
            .threshold(HarmBlockThreshold.Known.BLOCK_NONE)
            .build(),
        // 允许浪漫互动，但拦截纯色情内容
        SafetySetting.builder()
            .category(HarmCategory.Known.HARM_CATEGORY_SEXUALLY_EXPLICIT)
            .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
            .build(),
        // 拦截高风险的真实仇恨言论，但允许轻微的剧情需要
        SafetySetting.builder()
            .category(HarmCategory.Known.HARM_CATEGORY_HATE_SPEECH)
            .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
            .build(),
        // 防止过激的政治造谣
        SafetySetting.builder()
            .category(HarmCategory.Known.HARM_CATEGORY_CIVIC_INTEGRITY)
            .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
            .build()

*/


/*
    //计算 Token
    public Integer countTokens(List<Content> contents) {
        try {
            CountTokensResponse response = client.models.countTokens(
                    properties.getModel(),
                    contents,
                    null
            );
            return response.totalTokens().get();
        } catch (Exception e) {
            log.warn("Token计算失败: {}", e.getMessage());
            return 0;
        }
    }

*/