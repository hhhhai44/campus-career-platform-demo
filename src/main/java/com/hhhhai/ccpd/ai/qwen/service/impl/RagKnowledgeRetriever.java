package com.hhhhai.ccpd.ai.qwen.service.impl;

import com.hhhhai.ccpd.ai.core.port.KnowledgeRetriever;
import com.hhhhai.ccpd.ai.core.port.QuestionTypeSummarizer;
import com.hhhhai.ccpd.ai.core.port.VectorStoreClient;
import com.hhhhai.ccpd.ai.core.protocol.AgentChatRequest;
import com.hhhhai.ccpd.ai.core.protocol.AgentCitation;
import com.hhhhai.ccpd.ai.qwen.config.QwenProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG 知识检索实现（当前向量库未接入时自动降级为空结果）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagKnowledgeRetriever implements KnowledgeRetriever {

  private final VectorStoreClient vectorStoreClient;
  private final QwenProperties properties;

  @Override
  public RetrievedContext retrieve(AgentChatRequest request, String normalizedQuestion,
      QuestionTypeSummarizer.QuestionTypeSummary summary) {
    if (properties.getRag() == null || !properties.getRag().isEnabled()) {
      return RetrievedContext.builder().build();
    }

    int topK = Math.max(1, properties.getRag().getTopK());
    VectorStoreClient.SearchResult searchResult =
        vectorStoreClient.search(normalizedQuestion, summary, topK);

    List<AgentCitation> citations = searchResult.safeCitations();
    String context = searchResult.safeContext();
    if (!context.isBlank()) {
      context = "[知识检索上下文]\n" + context;
    }

    log.debug("RAG检索完成 category={} citations={} contextLength={}",
        summary == null ? "-" : summary.safeCategory(),
        citations.size(),
        context.length());

    return RetrievedContext.builder()
        .prependedContext(context)
        .citations(citations)
        .build();
  }
}

