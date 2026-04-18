package com.hhhhai.ccpd.ai.qwen.service.impl;

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
 * 使用大模型进行问题类型总结。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ModelQuestionTypeSummarizer implements QuestionTypeSummarizer {

  private final LlmClient llmClient;
  private final QwenProperties properties;

  @Override
  public QuestionTypeSummary summarize(AgentChatRequest request, String normalizedQuestion) {
    if (!StringUtils.hasText(normalizedQuestion)) {
      return new QuestionTypeSummary("通用问答", "问题为空，降级为通用类型");
    }
    if (properties.getRag() == null || !properties.getRag().isEnabled()) {
      return new QuestionTypeSummary("通用问答", "RAG流程关闭，使用默认类型");
    }

    try {
      AgentChatRequest classifyRequest = AgentChatRequest.builder()
          .model(StringUtils.hasText(properties.getRag().getClassifierModel())
              ? properties.getRag().getClassifierModel() : request.getModel())
          .systemPrompt("你是问题分类助手。请只输出一行：<问题类型>|<简短理由>。")
          .stream(false)
          .protocolProfile(AgentProtocolProfile.OPENAI_COMPATIBLE)
          .messages(List.of(AgentMessage.builder()
              .role(AgentMessageRole.USER)
              .content(normalizedQuestion)
              .build()))
          .metadata(Map.of("phase", "question-classification"))
          .build();

      AgentChatResponse response = llmClient.chat(classifyRequest);
      String raw = response == null ? "" : response.getAnswer();
      String[] parts = raw == null ? new String[0] : raw.split("\\|", 2);
      String category = parts.length > 0 && StringUtils.hasText(parts[0]) ? parts[0].trim() : "通用问答";
      String reason = parts.length > 1 && StringUtils.hasText(parts[1]) ? parts[1].trim() : "模型未返回理由";
      return new QuestionTypeSummary(category, reason);
    } catch (Exception ex) {
      log.warn("问题分类失败，降级为通用类型", ex);
      return new QuestionTypeSummary("通用问答", "分类失败，自动降级");
    }
  }
}

