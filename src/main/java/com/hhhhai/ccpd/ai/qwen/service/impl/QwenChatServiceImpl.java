package com.hhhhai.ccpd.ai.qwen.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.utils.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhhhai.ccpd.ai.qwen.config.QwenProperties;
import com.hhhhai.ccpd.ai.qwen.dto.QwenAskResponse;
import com.hhhhai.ccpd.ai.qwen.service.QwenChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 千问问答 Service（最小可用版）。
 *
 * <p>TODO(Agent): 后续可以把以下能力拆分为独立组件：
 * - 会话记忆（多轮 messages 管理、持久化）
 * - 知识库检索（RAG：向量库/全文检索）与引用返回
 * - 工具调用（函数调用/插件）
 * - 统一的 Prompt 模板与安全审计（敏感词、注入防护等）
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QwenChatServiceImpl implements QwenChatService {

  private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(20))
      .build();

  private static final String DEFAULT_COMPATIBLE_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";

  private final QwenProperties properties;
  private final ObjectMapper objectMapper;

  @Override
  public QwenAskResponse ask(String question) {
    if (!StringUtils.hasText(properties.getApiKey())) {
      // 这里用运行时异常交给全局异常处理也可以，但为了前端提示更明确，建议在 Controller 层做显式校验。
      throw new IllegalStateException("Qwen API Key 未配置");
    }

    // DashScope SDK 全局 Key（线程安全由 SDK 内部保证）
    // TODO(Qwen): 后续可改为更安全的 Key 管理方式（例如按租户/用户隔离，或使用 KMS）
    Constants.apiKey = properties.getApiKey();

    // TODO(Qwen): 如 SDK 支持自定义 baseUrl/endpoint，可在这里设置；当前先预留配置 properties.baseUrl
    // 例如（仅示意，字段名以 SDK 实际为准）：Constants.baseHttpApiUrl = properties.getBaseUrl();

    try {
      Generation gen = new Generation();

      List<Message> messages = new ArrayList<>();
      messages.add(Message.builder()
          .role(Role.SYSTEM.getValue())
          .content(properties.getSystemPrompt())
          .build());
      messages.add(Message.builder()
          .role(Role.USER.getValue())
          .content(question)
          .build());

      // 最小参数：model + messages
      // TODO(Qwen): 后续可暴露 temperature/top_p/max_tokens/seed 等参数到配置或请求体
      QwenParam param = QwenParam.builder()
          .model(properties.getModel())
          .messages(messages)
          .resultFormat(QwenParam.ResultFormat.MESSAGE)
          .build();

      GenerationResult result = gen.call(param);
      String answer = result.getOutput()
          .getChoices()
          .get(0)
          .getMessage()
          .getContent();

      return new QwenAskResponse(answer, properties.getModel());
    } catch (Exception e) {
      log.error("调用千问失败 model={} baseUrl={}", properties.getModel(), properties.getBaseUrl(), e);
      // 让全局异常处理兜底返回系统错误码；Controller 也可以包装为业务错误码
      throw new RuntimeException("调用千问失败", e);
    }
  }

  @Override
  public QwenAskResponse streamAsk(String question, Consumer<String> onDelta) {
    if (!StringUtils.hasText(properties.getApiKey())) {
      throw new IllegalStateException("Qwen API Key 未配置");
    }

    try {
      String streamUrl = resolveStreamUrl();
      String payload = objectMapper.writeValueAsString(buildStreamRequest(question));

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(streamUrl))
          .timeout(Duration.ofSeconds(120))
          .header("Authorization", "Bearer " + properties.getApiKey())
          .header("Content-Type", "application/json")
          .header("Accept", "text/event-stream")
          .POST(HttpRequest.BodyPublishers.ofString(payload))
          .build();

      HttpResponse<java.util.stream.Stream<String>> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofLines());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new IllegalStateException("调用千问流式接口失败，HTTP 状态码：" + response.statusCode());
      }

      StringBuilder answer = new StringBuilder();
      response.body().forEach(line -> handleSseLine(line, answer, onDelta));
      return new QwenAskResponse(answer.toString(), properties.getModel());
    } catch (Exception e) {
      log.error("调用千问流式接口失败 model={} baseUrl={}", properties.getModel(), properties.getBaseUrl(), e);
      throw new RuntimeException("调用千问流式接口失败", e);
    }
  }

  private Map<String, Object> buildStreamRequest(String question) {
    List<Map<String, String>> messages = new ArrayList<>();
    messages.add(Map.of("role", "system", "content", properties.getSystemPrompt()));
    messages.add(Map.of("role", "user", "content", question));

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("model", properties.getModel());
    body.put("messages", messages);
    body.put("stream", true);
    body.put("stream_options", Map.of("include_usage", true));
    return body;
  }

  private void handleSseLine(String rawLine, StringBuilder answer, Consumer<String> onDelta) {
    if (!StringUtils.hasText(rawLine) || !rawLine.startsWith("data:")) {
      return;
    }

    String data = rawLine.substring(5).trim();
    if (!StringUtils.hasText(data) || "[DONE]".equals(data)) {
      return;
    }

    try {
      JsonNode root = objectMapper.readTree(data);
      JsonNode choices = root.path("choices");
      if (!choices.isArray() || choices.isEmpty()) {
        return;
      }

      String content = choices.get(0)
          .path("delta")
          .path("content")
          .asText("");
      if (StringUtils.hasText(content)) {
        answer.append(content);
        onDelta.accept(content);
      }
    } catch (IOException parseEx) {
      log.warn("解析千问流式分片失败 raw={}", data, parseEx);
    }
  }

  private String resolveStreamUrl() {
    String base = StringUtils.hasText(properties.getBaseUrl())
        ? properties.getBaseUrl().trim()
        : DEFAULT_COMPATIBLE_BASE_URL;
    String normalized = base.replaceAll("/+$", "");
    if (normalized.endsWith("/chat/completions")) {
      return normalized;
    }
    return normalized + "/chat/completions";
  }
}
