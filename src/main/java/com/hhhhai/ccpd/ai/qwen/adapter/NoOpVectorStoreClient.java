package com.hhhhai.ccpd.ai.qwen.adapter;

import com.hhhhai.ccpd.ai.core.port.QuestionTypeSummarizer;
import com.hhhhai.ccpd.ai.core.port.VectorStoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 当前未接入向量库时的占位实现。
 */
@Slf4j
@Component
public class NoOpVectorStoreClient implements VectorStoreClient {

  @Override
  public SearchResult search(String normalizedQuestion,
      QuestionTypeSummarizer.QuestionTypeSummary summary,
      int topK) {
    log.debug("向量检索占位调用 question={} category={} topK={}",
        normalizedQuestion, summary == null ? "-" : summary.safeCategory(), topK);
    return SearchResult.empty();
  }
}

