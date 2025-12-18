package org.dromara.system.controller.system;

import lombok.RequiredArgsConstructor;

import org.dromara.system.domain.ChapterAnalysisResult;
import org.dromara.system.domain.ChapterSettlementRequest;
import org.dromara.system.domain.PlayRequest;
import org.dromara.system.service.StoryEngineService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import cn.dev33.satoken.annotation.SaIgnore;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

@Slf4j
@RestController
@RequestMapping("/api/story")
@RequiredArgsConstructor
public class StoryController {

    private final StoryEngineService storyEngineService;
    // 用于异步执行流式任务，避免阻塞 Servlet 线程
    private final ExecutorService nonBlockingService = Executors.newCachedThreadPool();

    /**
     * 接口：交互式剧情生成，根据用户输入，流式生成下一段剧情文本。利用 SSE 技术实现打字机效果。
     * <p>
     * 对应业务场景：玩家输入行动或指令，AI 生成后续剧情。
     * <p>
     * <strong>关于用户身份的特殊说明：</strong><br>
     * 用户的输入 (userAction) 被视为“带着导演帽子的主演”。
     * <ul>
     *     <li>如果输入是 <i>"我拔出长剑冲向恶龙"</i> -> AI 将其视为角色行动进行判定和描写。</li>
     *     <li>如果输入是 <i>"跳过这段路程，直接到达城堡，并且天气突然转阴"</i> -> AI 将其视为导演指令，直接推动剧情发展。</li>
     * </ul>
     *
     * @param request 包含当前上下文、角色状态、历史记录和用户最新输入的请求体
     * @return SseEmitter 服务器发送事件流，客户端需通过 EventSource 接收 text/event-stream
     */
    @SaIgnore
    @PostMapping(value = "/play/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter continueStory(@RequestBody PlayRequest request) {
        // 设置超时时间，例如 5 分钟 (取决于生成长度)
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        // 在独立线程中执行生成逻辑，释放 Servlet 容器线程
        nonBlockingService.execute(() -> {
            try {
                log.info("Starting story generation for Novel ID: {}", request.getNovelId());

                storyEngineService.continueStory(
                        request,
                        // 1. onChunk: 接收到 AI 的一部分文本片段
                        (chunk) -> {
                            try {
                                // 发送数据给前端
                                emitter.send(SseEmitter.event().data(chunk));
                            } catch (IOException e) {
                                log.error("Error sending SSE chunk", e);
                                emitter.completeWithError(e);
                            }
                        },
                        // 2. onComplete: 生成结束
                        () -> {
                            try {
                                // 发送一个特殊的结束标记，方便前端处理
                                emitter.send(SseEmitter.event().name("close").data("[DONE]"));
                            } catch (IOException e) {
                                // ignore
                            }
                            log.info("Story generation completed...");
                            emitter.complete();
                        }
                );
            } catch (Exception e) {
                log.error("Critical error in generation thread", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 接口：章节结算与状态压缩 (同步响应)
     * <p>
     * 对应业务场景：当一章结束后（由前端判断或达到字数限制），分析本章文本，压缩生成新的前文摘要，并更新所有角色的属性状态。
     * <p>
     * <strong>核心逻辑：</strong>
     * AI 阅读本章产生的全部文本，执行以下操作：
     * <ol>
     *     <li><strong>摘要更新：</strong>将本章发生的大事合并进 {@code previousSummary}。</li>
     *     <li><strong>状态更新：</strong>分析所有角色的最新状态（受伤？获得道具？位置改变？），生成新的 JSON 属性。</li>
     * </ol>
     * <p>
     * 注意：此接口不生成给用户看的小说文本，而是生成给系统用的结构化数据。
     *
     * @param request 包含完整章节文本和旧状态的请求体
     * @return 结构化的分析结果 (新的摘要 + 角色属性列表)
     */
    @SaIgnore
    @PostMapping("/chapter/conclude")
    public ResponseEntity<ChapterAnalysisResult> concludeChapter(@RequestBody ChapterSettlementRequest request) {

        log.info("Concluding chapter. Input text length: {}", request.getPureStoryText().length());

        ChapterAnalysisResult result = storyEngineService.concludeChapter(
                request.getContext(),
                request.getCharacters(),
                request.getPureStoryText()
        );

        return ResponseEntity.ok(result);
    }
}
/*
关于 Implicit Caching (隐式缓存)
Google GenAI 的缓存机制依赖于 Prompt 前缀的一致性。为了最大化性能：

World Setting (世界观) 和 Main Storyline (主线) 的文本必须在整个小说游玩过程中保持 完全一致（连空格都不要变）。
StoryEngineService 已经将这两部分放在了 System Prompt 的最前面。
前端在构造 PlayRequest 时，不要每次都微调 context.worldSetting，除非用户显式修改了设定。
1. 流式生成 (/api/story/play/stream)
前端处理逻辑：

使用 EventSource (JS) 或 fetch + ReadableStream 连接接口。
接收到的数据是纯文本片段，直接 append 到页面显示，形成打字机效果。
用户输入既可以是 "我拔剑" (Action)，也可以是 "突然天降大雨" (Director Command)。
注意：每次请求都需要带上 currentChapterHistory（本章的历史对话），但不需要带上上一章的对话（上一章的内容应当已经压缩进 context.previousSummary）。
2. 章节结算 (/api/story/chapter/conclude)
前端处理逻辑：

何时调用：当用户点击“进入下一章”或者本章对话轮数达到上限（如 20 轮）时。
数据组装：前端需将本章流式生成的每一段 AI 回复拼接成一个完整的字符串 pureStoryText。
结果处理：
接口返回 newSummary -> 前端更新本地存储的 context.previousSummary。
接口返回 characterUpdates -> 前端根据人名匹配，更新本地 characters 列表中的 currentAttributes。
清空 currentChapterHistory，准备开始新的一章。

*/