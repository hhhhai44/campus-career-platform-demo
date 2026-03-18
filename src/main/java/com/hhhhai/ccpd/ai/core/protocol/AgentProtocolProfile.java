package com.hhhhai.ccpd.ai.core.protocol;

/**
 * 类的含义：Agent 协议档位枚举。
 * 类的作用：声明当前请求使用的协议兼容类型，为多框架与多协议扩展预留入口。
 */
public enum AgentProtocolProfile {
  OPENAI_COMPATIBLE,
  DASHSCOPE_COMPATIBLE,
  MCP_BRIDGE_RESERVED
}
