package com.hhhhai.ccpd.ai.core.port;

import com.hhhhai.ccpd.ai.core.protocol.AgentChatRequest;

import java.util.Collections;
import java.util.Map;

/**
 * 类的含义：工具调用执行接口。
 * 类的作用：为 Agent 工具/函数调用构建执行上下文并返回可注入模型的工具结果。
 */
public interface ToolExecutor {

  ToolExecutor NO_OP = request -> Collections.emptyMap();

  Map<String, Object> buildToolContext(AgentChatRequest request);
}
