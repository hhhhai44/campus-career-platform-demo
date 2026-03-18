package com.hhhhai.ccpd.ai.qwen.adapter;

import com.hhhhai.ccpd.ai.core.port.LlmClient;
import com.hhhhai.ccpd.ai.core.protocol.AgentChatRequest;
import com.hhhhai.ccpd.ai.core.protocol.AgentChatResponse;
import com.hhhhai.ccpd.ai.core.protocol.AgentMessage;
import com.hhhhai.ccpd.ai.core.protocol.AgentMessageRole;
import com.hhhhai.ccpd.ai.core.protocol.AgentUsage;
import com.hhhhai.ccpd.ai.qwen.config.QwenProperties;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * 类的含义：DashScope 兼容协议模型适配器。
 * 类的作用：负责将统一 Agent 协议请求转换为 LangChain4j OpenAI 兼容调用，并解析同步/流式响应。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QwenCompatibleLlmClient implements LlmClient {

  private static final String DEFAULT_COMPATIBLE_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";

  private final QwenProperties properties;

  @Override
  public AgentChatResponse chat(AgentChatRequest request) {
    ensureApiKey();
    try {
      OpenAiChatModel model = OpenAiChatModel.builder()
          .apiKey(properties.getApiKey())
          .baseUrl(resolveCompatibleBaseUrl())
          .modelName(request.getModel())
          .timeout(Duration.ofSeconds(120))
          .build();

      ChatResponse response = model.chat(buildChatRequest(request));
      String answer = response.aiMessage() == null ? "" : response.aiMessage().text();

      return AgentChatResponse.builder()
          .answer(answer)
          .model(request.getModel())
          .finishReason(response.finishReason() == null ? null : response.finishReason().name())
          .usage(parseUsage(response.tokenUsage()))
          .extensions(Map.of("provider", providerName()))
          .build();
    } catch (Exception e) {
      log.error("调用智能问答模型失败 model={} baseUrl={}", request.getModel(), properties.getBaseUrl(), e);
      throw new RuntimeException("调用智能问答模型失败", e);
    }
  }

  @Override
  public AgentChatResponse streamChat(AgentChatRequest request, Consumer<String> onDelta) {
    ensureApiKey();
    try {
      OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
          .apiKey(properties.getApiKey())
          .baseUrl(resolveCompatibleBaseUrl())
          .modelName(request.getModel())
          .timeout(Duration.ofSeconds(120))
          .build();

      StringBuilder answerBuilder = new StringBuilder();
      AtomicReference<Response<AiMessage>> finalResponseRef = new AtomicReference<>();
      AtomicReference<Throwable> errorRef = new AtomicReference<>();
      CountDownLatch latch = new CountDownLatch(1);

      model.generate(buildMessages(request), new StreamingResponseHandler<>() {
        @Override
        public void onNext(String token) {
          if (!StringUtils.hasText(token)) {
            return;
          }
          answerBuilder.append(token);
          onDelta.accept(token);
        }

        @Override
        public void onComplete(Response<AiMessage> completeResponse) {
          finalResponseRef.set(completeResponse);
          latch.countDown();
        }

        @Override
        public void onError(Throwable error) {
          errorRef.set(error);
          latch.countDown();
        }
      });

      boolean completed = latch.await(130, TimeUnit.SECONDS);
      if (!completed) {
        throw new IllegalStateException("智能问答流式响应超时");
      }
      if (errorRef.get() != null) {
        throw new RuntimeException(errorRef.get());
      }

      Response<AiMessage> finalResponse = finalResponseRef.get();
      AgentUsage usage = finalResponse == null ? null : parseUsage(finalResponse.tokenUsage());
      String finishReason = finalResponse == null || finalResponse.finishReason() == null
          ? null
          : finalResponse.finishReason().name();

      return AgentChatResponse.builder()
          .answer(answerBuilder.toString())
          .model(request.getModel())
          .finishReason(finishReason)
          .usage(usage)
          .extensions(Map.of("provider", providerName(), "stream", true))
          .build();
    } catch (Exception e) {
      log.error("调用智能问答流式接口失败 model={} baseUrl={}", request.getModel(), properties.getBaseUrl(), e);
      throw new RuntimeException("调用智能问答流式接口失败", e);
    }
  }

  @Override
  public String providerName() {
    return "langchain4j-openai-compatible";
  }

  private ChatRequest buildChatRequest(AgentChatRequest request) {
    return ChatRequest.builder()
        .messages(buildMessages(request))
        .build();
  }

  private List<ChatMessage> buildMessages(AgentChatRequest request) {
    List<ChatMessage> messages = new ArrayList<>();
    if (StringUtils.hasText(request.getSystemPrompt())) {
      messages.add(SystemMessage.from(request.getSystemPrompt()));
    }

    if (request.getMessages() == null) {
      return messages;
    }

    for (AgentMessage message : request.getMessages()) {
      if (message == null || !StringUtils.hasText(message.content())) {
        continue;
      }
      AgentMessageRole role = message.role() == null ? AgentMessageRole.USER : message.role();
      messages.add(toLangchainMessage(role, message.content()));
    }
    return messages;
  }

  private ChatMessage toLangchainMessage(AgentMessageRole role, String content) {
    return switch (role) {
      case SYSTEM -> SystemMessage.from(content);
      case ASSISTANT -> AiMessage.from(content);
      case TOOL -> UserMessage.from("[TOOL]\n" + content);
      case USER -> UserMessage.from(content);
    };
  }

  private AgentUsage parseUsage(TokenUsage tokenUsage) {
    if (tokenUsage == null) {
      return null;
    }
    Integer promptTokens = tokenUsage.inputTokenCount();
    Integer completionTokens = tokenUsage.outputTokenCount();
    Integer totalTokens = tokenUsage.totalTokenCount();
    if (promptTokens == null && completionTokens == null && totalTokens == null) {
      return null;
    }
    return AgentUsage.builder()
        .promptTokens(promptTokens)
        .completionTokens(completionTokens)
        .totalTokens(totalTokens)
        .build();
  }

  private String resolveCompatibleBaseUrl() {
    String base = StringUtils.hasText(properties.getBaseUrl())
        ? properties.getBaseUrl().trim()
        : DEFAULT_COMPATIBLE_BASE_URL;
    return base.replaceAll("/+$", "");
  }

  private void ensureApiKey() {
    if (!StringUtils.hasText(properties.getApiKey())) {
      throw new IllegalStateException("智能问答 API Key 未配置");
    }
  }
}
