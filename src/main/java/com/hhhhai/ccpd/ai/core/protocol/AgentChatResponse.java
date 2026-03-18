package com.hhhhai.ccpd.ai.core.protocol;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

/**
 * 类的含义：智能问答统一响应对象。
 * 类的作用：统一返回回答内容、模型信息、用量与扩展字段，便于上层处理。
 */
@Value
@Builder
public class AgentChatResponse {
  String answer;
  String model;
  String finishReason;
  AgentUsage usage;
  List<AgentCitation> citations;

  /**
   * 扩展字段：用于预留工具轨迹、推理摘要、安全策略判定等附加信息。
   */
  Map<String, Object> extensions;
}
