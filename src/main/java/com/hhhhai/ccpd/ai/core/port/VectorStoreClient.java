package com.hhhhai.ccpd.ai.core.port;

import com.hhhhai.ccpd.ai.core.protocol.AgentCitation;
import java.util.Collections;
import java.util.List;

/**
 * 类的含义：向量检索客户端接口。
 * 类的作用：封装向量数据库检索能力，便于后续替换 Milvus/PGVector/ES 等实现。
 */
public interface VectorStoreClient {

  VectorStoreClient NO_OP = (question, summary, topK) -> SearchResult.empty();

  SearchResult search(String normalizedQuestion,
      QuestionTypeSummarizer.QuestionTypeSummary summary,
      int topK);

  record SearchResult(List<AgentCitation> citations, String mergedContext) {

    public static SearchResult empty() {
      return new SearchResult(Collections.emptyList(), "");
    }

    public List<AgentCitation> safeCitations() {
      return citations == null ? Collections.emptyList() : citations;
    }

    public String safeContext() {
      return mergedContext == null ? "" : mergedContext;
    }
  }
}

