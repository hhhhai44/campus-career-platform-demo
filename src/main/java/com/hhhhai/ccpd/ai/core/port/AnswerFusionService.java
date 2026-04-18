package com.hhhhai.ccpd.ai.core.port;

import com.hhhhai.ccpd.ai.core.protocol.AgentChatRequest;

/**
 * 类的含义：答案融合接口。
 * 类的作用：将原始问题与检索知识融合，产出给最终回答模型的增强问题。
 */
public interface AnswerFusionService {

  AnswerFusionService NO_OP = (request, question, summary, context) -> question;

  String fuse(AgentChatRequest request, String normalizedQuestion,
      QuestionTypeSummarizer.QuestionTypeSummary summary,
      KnowledgeRetriever.RetrievedContext context);
}

