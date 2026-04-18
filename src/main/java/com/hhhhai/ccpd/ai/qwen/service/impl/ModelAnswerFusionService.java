package com.hhhhai.ccpd.ai.qwen.service.impl;

import com.hhhhai.ccpd.ai.core.port.AnswerFusionService;
import com.hhhhai.ccpd.ai.core.port.KnowledgeRetriever;
import com.hhhhai.ccpd.ai.core.port.LlmClient;
import com.hhhhai.ccpd.ai.core.port.QuestionTypeSummarizer;
import com.hhhhai.ccpd.ai.core.protocol.AgentChatRequest;
import com.hhhhai.ccpd.ai.core.protocol.AgentChatResponse;
import com.hhhhai.ccpd.ai.core.protocol.AgentMessage;
import com.hhhhai.ccpd.ai.core.protocol.AgentMessageRole;
import com.hhhhai.ccpd.ai.core.protocol.AgentProtocolProfile;
import com.hhhhai.ccpd.ai.qwen.config.QwenProperties;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 使用大模型融合问题与知识上下文，输出最终回答提示内容。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ModelAnswerFusionService implements AnswerFusionService {

  private final LlmClient llmClient;
  private final QwenProperties properties;

  @Override
  public String fuse(AgentChatRequest request, String normalizedQuestion,
      QuestionTypeSummarizer.QuestionTypeSummary summary,
      KnowledgeRetriever.RetrievedContext context) {
    if (!StringUtils.hasText(normalizedQuestion)) {
      return normalizedQuestion;
    }
    if (properties.getRag() == null || !properties.getRag().isEnabled()) {
      return normalizedQuestion;
    }

    String contextText = context == null ? "" : context.safeContext();
    if (!StringUtils.hasText(contextText)) {
      return normalizedQuestion;
    }

    try {
      String prompt = "请将【用户问题】与【知识上下文】融合为最终回答输入。\n"
          + "要求：保留问题核心意图，提炼上下文关键点，不编造未提供事实。\n"
          + "问题类型：" + (summary == null ? "通用问答" : summary.safeCategory()) + "\n\n"
          + "【用户问题】\n" + normalizedQuestion + "\n\n"
          + "【知识上下文】\n" + contextText + "\n\n"
          + "请输出融合后的提问文本。";

      AgentChatRequest fusionRequest = AgentChatRequest.builder()
          .model(StringUtils.hasText(properties.getRag().getFusionModel())
              ? properties.getRag().getFusionModel() : request.getModel())
          .systemPrompt("你是问答编排助手。")
          .stream(false)
          .protocolProfile(AgentProtocolProfile.OPENAI_COMPATIBLE)
          .messages(List.of(AgentMessage.builder()
              .role(AgentMessageRole.USER)
              .content(prompt)
              .build()))
          .metadata(Map.of("phase", "answer-fusion"))
          .build();

      AgentChatResponse response = llmClient.chat(fusionRequest);
      String fused = response == null ? null : response.getAnswer();
      return StringUtils.hasText(fused) ? fused.trim() : normalizedQuestion;
    } catch (Exception ex) {
      log.warn("答案融合失败，回退到原始问题", ex);
      return normalizedQuestion;
    }
  }
}


