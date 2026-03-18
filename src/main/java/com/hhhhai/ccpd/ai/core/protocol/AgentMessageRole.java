package com.hhhhai.ccpd.ai.core.protocol;

/**
 * 类的含义：智能问答消息角色枚举。
 * 类的作用：统一不同协议中的角色标识，便于在模型请求中做角色映射。
 */
public enum AgentMessageRole {
  SYSTEM("system"),
  USER("user"),
  ASSISTANT("assistant"),
  TOOL("tool");

  private final String wireValue;

  AgentMessageRole(String wireValue) {
    this.wireValue = wireValue;
  }

  public String getWireValue() {
    return wireValue;
  }
}
