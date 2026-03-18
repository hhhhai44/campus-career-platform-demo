package com.hhhhai.ccpd.ai.qwen.service;

import com.hhhhai.ccpd.ai.qwen.dto.QwenAskResponse;

import java.util.function.Consumer;

public interface QwenChatService {
  QwenAskResponse ask(String question);

  QwenAskResponse streamAsk(String question, Consumer<String> onDelta);

  // TODO(Agent): 后续可扩展为多轮对话接口（messages/history），并加入会话记忆（sessionId -> messages）
}


