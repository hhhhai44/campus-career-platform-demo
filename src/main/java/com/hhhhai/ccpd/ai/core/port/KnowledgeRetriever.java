package com.hhhhai.ccpd.ai.core.port;

import com.hhhhai.ccpd.ai.core.protocol.AgentChatRequest;
import com.hhhhai.ccpd.ai.core.protocol.AgentCitation;
import lombok.Builder;

import java.util.Collections;
import java.util.List;

/**
 * 类的含义：知识检索接口。
 * 类的作用：为 RAG 场景提供外部知识检索与上下文拼装能力。
 */
public interface KnowledgeRetriever {

  KnowledgeRetriever NO_OP = (request, question, summary) -> RetrievedContext.builder().build();

  RetrievedContext retrieve(AgentChatRequest request, String normalizedQuestion,
      QuestionTypeSummarizer.QuestionTypeSummary summary);

  @Builder
  record RetrievedContext(String prependedContext, List<AgentCitation> citations) {
    public String safeContext() {
      return prependedContext == null ? "" : prependedContext;
    }

    public List<AgentCitation> safeCitations() {
      return citations == null ? Collections.emptyList() : citations;
    }
  }
}
