package com.hhhhai.ccpd.ai.qwen.service.impl;

import com.hhhhai.ccpd.ai.core.application.AgentChatOrchestrator;
import com.hhhhai.ccpd.ai.core.protocol.AgentChatRequest;
import com.hhhhai.ccpd.ai.core.protocol.AgentChatResponse;
import com.hhhhai.ccpd.ai.core.protocol.AgentMessage;
import com.hhhhai.ccpd.ai.core.protocol.AgentMessageRole;
import com.hhhhai.ccpd.ai.core.protocol.AgentProtocolProfile;
import com.hhhhai.ccpd.ai.qwen.config.QwenProperties;
import com.hhhhai.ccpd.ai.qwen.dto.QwenAskResponse;
import com.hhhhai.ccpd.ai.qwen.service.QwenChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 智能问答 Service（兼容原有接口）。
 */
@Service
@RequiredArgsConstructor
public class QwenChatServiceImpl implements QwenChatService {

  private final AgentChatOrchestrator orchestrator;
  private final QwenProperties properties;

  @Override
  public QwenAskResponse streamAsk(String question, Consumer<String> onDelta) {
    ensureApiKey();
    AgentChatRequest request = buildSingleTurnRequest(question, true);
    AgentChatResponse response = orchestrator.streamChat(request, onDelta);
    return new QwenAskResponse(response.getAnswer(), response.getModel());
  }

  private AgentChatRequest buildSingleTurnRequest(String question, boolean stream) {
    return AgentChatRequest.builder()
        .model(properties.getModel())
        .systemPrompt(properties.getSystemPrompt())
        .stream(stream)
        .protocolProfile(AgentProtocolProfile.OPENAI_COMPATIBLE)
        .messages(List.of(AgentMessage.builder()
            .role(AgentMessageRole.USER)
            .content(question)
            .build()))
        // 预留 Agent 运行时扩展参数（租户、追踪、策略、工具路由等）。
        .metadata(Map.of())
        .build();
  }

  private void ensureApiKey() {
    if (!StringUtils.hasText(properties.getApiKey())) {
      throw new IllegalStateException("智能问答 API Key 未配置");
    }
  }
}
