package com.hhhhai.ccpd.ai.core.port;

import com.hhhhai.ccpd.ai.core.protocol.AgentChatRequest;
import com.hhhhai.ccpd.ai.core.protocol.AgentChatResponse;

import java.util.function.Consumer;

/**
 * 类的含义：大模型调用适配接口。
 * 类的作用：定义统一的同步与流式调用能力，屏蔽具体厂商 SDK 或 HTTP 细节。
 */
public interface LlmClient {

  AgentChatResponse chat(AgentChatRequest request);

  AgentChatResponse streamChat(AgentChatRequest request, Consumer<String> onDelta);

  default String providerName() {
    return "unknown";
  }
}
