package com.hhhhai.ccpd.ai.core.protocol;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

/**
 * 类的含义：智能问答统一请求对象。
 * 类的作用：屏蔽底层模型差异，封装模型参数、消息列表与扩展元数据。
 */
@Value
@Builder
public class AgentChatRequest {
  String model;
  String systemPrompt;
  String sessionId;
  boolean stream;
  AgentProtocolProfile protocolProfile;
  List<AgentMessage> messages;

  /**
   * 扩展元数据：用于预留工具配置、策略标签、租户信息、链路追踪等框架级参数。
   */
  Map<String, Object> metadata;
}
