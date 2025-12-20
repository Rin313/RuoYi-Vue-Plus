package org.dromara.system.controller;

import lombok.RequiredArgsConstructor;

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

@Slf4j
@RestController
@RequestMapping("/api/story")
@RequiredArgsConstructor
public class GenController {

    private final StoryEngineService storyEngineService;
    // 用于异步执行流式任务，避免阻塞 Servlet 线程
    private final ExecutorService nonBlockingService = Executors.newCachedThreadPool();

    /**
     * 交互式剧情生成，根据用户输入，流式生成下一段剧情文本。利用 SSE 技术实现打字机效果。
     */
    @SaIgnore
    @PostMapping(value = "/play/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter continueStory(@RequestBody PlayRequest request) {
        // 设置超时时间
        SseEmitter emitter = new SseEmitter(3 * 60 * 1000L);
        // 在独立线程中执行生成逻辑，释放 Servlet 容器线程
        nonBlockingService.execute(() -> {
            try {
                storyEngineService.continueStory(
                        request.getChapterId(),request.getUserAction(),
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
     * 章节结算与状态压缩 (同步响应，避免没有结算完就进入下一章)
     */
    @SaIgnore
    @PostMapping("/chapter/conclude/{chapterId}")
    public void concludeChapter(@PathVariable Long chapterId) {
        storyEngineService.concludeChapter(chapterId);
    }
}