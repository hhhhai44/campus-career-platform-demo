package com.hhhhai.ccpd.ai.core.port;

import com.hhhhai.ccpd.ai.core.protocol.AgentChatRequest;

/**
 * 类的含义：问题类型分析接口。
 * 类的作用：对用户问题进行类型总结，供后续检索与答案融合阶段使用。
 */
public interface QuestionTypeSummarizer {

  QuestionTypeSummarizer NO_OP = (request, question) ->
      new QuestionTypeSummary("通用问答", "未启用分类，使用默认通用类型");

  QuestionTypeSummary summarize(AgentChatRequest request, String normalizedQuestion);

  record QuestionTypeSummary(String category, String reason) {

    public String safeCategory() {
      return category == null || category.isBlank() ? "通用问答" : category;
    }

    public String safeReason() {
      return reason == null ? "" : reason;
    }
  }
}

