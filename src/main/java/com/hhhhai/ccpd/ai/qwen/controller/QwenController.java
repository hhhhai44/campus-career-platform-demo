package com.hhhhai.ccpd.ai.qwen.controller;

import com.hhhhai.ccpd.ai.qwen.config.QwenProperties;
import com.hhhhai.ccpd.ai.qwen.dto.QwenAskRequest;
import com.hhhhai.ccpd.ai.qwen.dto.QwenAskResponse;
import com.hhhhai.ccpd.ai.qwen.service.QwenChatService;
import com.hhhhai.ccpd.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 千问问答接口（最小可用）。
 *
 * <p>路径建议保持独立：/qwen/**，避免与论坛/资源模块耦合。</p>
 * <p>TODO(Agent): 后续可新增 /qwen/chat（多轮）、/qwen/agent（工具/知识库代理）、/qwen/knowledge-base（知识库管理）等。</p>
 */
@Slf4j
@RestController
@RequestMapping("/qwen")
@RequiredArgsConstructor
public class QwenController {

  private final QwenChatService qwenChatService;
  private final QwenProperties properties;

  @PostMapping("/ask")
  public Result<QwenAskResponse> ask(@Valid @RequestBody QwenAskRequest req) {
    if (!StringUtils.hasText(properties.getApiKey())) {
      return Result.error("未配置千问 API Key：请设置环境变量 DASHSCOPE_API_KEY（或 CCPD_QWEN_API_KEY）");
    }
    QwenAskResponse resp = qwenChatService.ask(req.getQuestion());
    return Result.success(resp);
  }

  @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter askStream(@Valid @RequestBody QwenAskRequest req) {
    SseEmitter emitter = new SseEmitter(0L);

    if (!StringUtils.hasText(properties.getApiKey())) {
      sendEvent(emitter, "error", "未配置千问 API Key：请设置环境变量 DASHSCOPE_API_KEY（或 CCPD_QWEN_API_KEY）");
      emitter.complete();
      return emitter;
    }

    CompletableFuture.runAsync(() -> {
      try {
        QwenAskResponse resp = qwenChatService.streamAsk(req.getQuestion(),
            delta -> sendEvent(emitter, "delta", delta));
        sendEvent(emitter, "done", Map.of("model", resp.getModel()));
        emitter.complete();
      } catch (Exception e) {
        log.error("流式问答失败", e);
        sendEvent(emitter, "error", "调用千问失败，请稍后重试");
        emitter.complete();
      }
    });

    return emitter;
  }

  private void sendEvent(SseEmitter emitter, String eventName, Object data) {
    try {
      emitter.send(SseEmitter.event().name(eventName).data(data));
    } catch (IOException e) {
      throw new IllegalStateException("SSE 事件发送失败", e);
    }
  }
}
