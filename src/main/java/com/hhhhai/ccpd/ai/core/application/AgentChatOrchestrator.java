package com.hhhhai.ccpd.ai.core.application;

import com.hhhhai.ccpd.ai.core.port.AiTelemetry;
import com.hhhhai.ccpd.ai.core.port.AnswerFusionService;
import com.hhhhai.ccpd.ai.core.port.AuditService;
import com.hhhhai.ccpd.ai.core.port.KnowledgeRetriever;
import com.hhhhai.ccpd.ai.core.port.LlmClient;
import com.hhhhai.ccpd.ai.core.port.MemoryStore;
import com.hhhhai.ccpd.ai.core.port.QuestionTypeSummarizer;
import com.hhhhai.ccpd.ai.core.port.ToolExecutor;
import com.hhhhai.ccpd.ai.core.protocol.AgentChatRequest;
import com.hhhhai.ccpd.ai.core.protocol.AgentChatResponse;
import com.hhhhai.ccpd.ai.core.protocol.AgentCitation;
import com.hhhhai.ccpd.ai.core.protocol.AgentMessage;
import com.hhhhai.ccpd.ai.core.protocol.AgentMessageRole;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 类的含义：Agent 对话编排类。
 * 类的作用：串联协议标准化、扩展点调用与模型适配器调用，形成统一问答执行链路。
 */
@Service
@RequiredArgsConstructor
public class AgentChatOrchestrator {

  private final LlmClient llmClient;
  private final ObjectProvider<MemoryStore> memoryStoreProvider;
  private final ObjectProvider<KnowledgeRetriever> knowledgeRetrieverProvider;
  private final ObjectProvider<QuestionTypeSummarizer> questionTypeSummarizerProvider;
  private final ObjectProvider<AnswerFusionService> answerFusionServiceProvider;
  private final ObjectProvider<ToolExecutor> toolExecutorProvider;
  private final ObjectProvider<AuditService> auditServiceProvider;
  private final ObjectProvider<AiTelemetry> telemetryProvider;

  public AgentChatResponse chat(AgentChatRequest request) {
    AgentChatRequest enriched = enrichRequest(request);
    Instant start = Instant.now();
    AuditService audit = auditServiceProvider.getIfAvailable(() -> AuditService.NO_OP);
    AiTelemetry telemetry = telemetryProvider.getIfAvailable(() -> AiTelemetry.NO_OP);

    audit.beforeInvoke(enriched);
    try {
      AgentChatResponse response = llmClient.chat(enriched);
      persistConversation(enriched, response);
      audit.afterInvoke(enriched, response);
      telemetry.recordSuccess(llmClient.providerName(), response.getModel(), false, elapsedMs(start));
      return response;
    } catch (Exception ex) {
      audit.onError(enriched, ex);
      telemetry.recordFailure(llmClient.providerName(), enriched.getModel(), false, elapsedMs(start), ex);
      throw ex;
    }
  }

  public AgentChatResponse streamChat(AgentChatRequest request, Consumer<String> onDelta) {
    AgentChatRequest enriched = enrichRequest(request);
    Instant start = Instant.now();
    AuditService audit = auditServiceProvider.getIfAvailable(() -> AuditService.NO_OP);
    AiTelemetry telemetry = telemetryProvider.getIfAvailable(() -> AiTelemetry.NO_OP);

    audit.beforeInvoke(enriched);
    try {
      AgentChatResponse response = llmClient.streamChat(enriched, onDelta);
      persistConversation(enriched, response);
      audit.afterInvoke(enriched, response);
      telemetry.recordSuccess(llmClient.providerName(), response.getModel(), true, elapsedMs(start));
      return response;
    } catch (Exception ex) {
      audit.onError(enriched, ex);
      telemetry.recordFailure(llmClient.providerName(), enriched.getModel(), true, elapsedMs(start), ex);
      throw ex;
    }
  }

  private AgentChatRequest enrichRequest(AgentChatRequest request) {
    MemoryStore memoryStore = memoryStoreProvider.getIfAvailable(() -> MemoryStore.NO_OP);
    KnowledgeRetriever retriever = knowledgeRetrieverProvider.getIfAvailable(() -> KnowledgeRetriever.NO_OP);
    QuestionTypeSummarizer summarizer =
        questionTypeSummarizerProvider.getIfAvailable(() -> QuestionTypeSummarizer.NO_OP);
    AnswerFusionService fusionService =
        answerFusionServiceProvider.getIfAvailable(() -> AnswerFusionService.NO_OP);
    ToolExecutor toolExecutor = toolExecutorProvider.getIfAvailable(() -> ToolExecutor.NO_OP);

    List<AgentMessage> mergedMessages = new ArrayList<>();
    if (StringUtils.hasText(request.getSessionId())) {
      mergedMessages.addAll(memoryStore.load(request.getSessionId()));
    }
    mergedMessages.addAll(safeMessages(request));

    String normalizedQuestion = extractLatestUserQuestion(request);
    QuestionTypeSummarizer.QuestionTypeSummary summary = summarizer.summarize(request, normalizedQuestion);
    KnowledgeRetriever.RetrievedContext context = retriever.retrieve(request, normalizedQuestion, summary);
    if (context == null) {
      context = KnowledgeRetriever.RetrievedContext.builder().build();
    }

    String fusedQuestion = fusionService.fuse(request, normalizedQuestion, summary, context);
    if (StringUtils.hasText(fusedQuestion)) {
      replaceLastUserMessageContent(mergedMessages, fusedQuestion);
    }

    if (StringUtils.hasText(context.safeContext())) {
      mergedMessages.add(0, AgentMessage.builder()
          .role(AgentMessageRole.SYSTEM)
          .content("[RAG_CONTEXT]\n" + context.safeContext())
          .build());
    }

    Map<String, Object> metadata = new HashMap<>();
    if (request.getMetadata() != null) {
      metadata.putAll(request.getMetadata());
    }
    metadata.put("toolContext", toolExecutor.buildToolContext(request));
    metadata.put("questionCategory", summary.safeCategory());
    metadata.put("questionCategoryReason", summary.safeReason());
    metadata.put("citations", context.safeCitations().stream().map(AgentCitation::sourceId).toList());

    return AgentChatRequest.builder()
        .model(request.getModel())
        .systemPrompt(request.getSystemPrompt())
        .sessionId(request.getSessionId())
        .stream(request.isStream())
        .protocolProfile(request.getProtocolProfile())
        .messages(mergedMessages)
        .metadata(metadata)
        .build();
  }

  private void persistConversation(AgentChatRequest request, AgentChatResponse response) {
    if (!StringUtils.hasText(request.getSessionId())) {
      return;
    }
    MemoryStore memoryStore = memoryStoreProvider.getIfAvailable(() -> MemoryStore.NO_OP);
    List<AgentMessage> delta = new ArrayList<>(safeMessages(request));
    delta.add(AgentMessage.builder().role(AgentMessageRole.ASSISTANT).content(response.getAnswer()).build());
    memoryStore.save(request.getSessionId(), delta);
  }

  private List<AgentMessage> safeMessages(AgentChatRequest request) {
    if (request.getMessages() == null) {
      return Collections.emptyList();
    }
    return request.getMessages();
  }

  private String extractLatestUserQuestion(AgentChatRequest request) {
    List<AgentMessage> messages = safeMessages(request);
    for (int i = messages.size() - 1; i >= 0; i--) {
      AgentMessage message = messages.get(i);
      if (message != null
          && message.role() == AgentMessageRole.USER
          && StringUtils.hasText(message.content())) {
        return message.content().trim();
      }
    }
    return "";
  }

  private void replaceLastUserMessageContent(List<AgentMessage> messages, String question) {
    for (int i = messages.size() - 1; i >= 0; i--) {
      AgentMessage message = messages.get(i);
      if (message != null
          && message.role() == AgentMessageRole.USER
          && StringUtils.hasText(message.content())) {
        messages.set(i, AgentMessage.builder()
            .role(AgentMessageRole.USER)
            .content(question)
            .build());
        return;
      }
    }
  }

  private long elapsedMs(Instant start) {
    return Duration.between(start, Instant.now()).toMillis();
  }
}
