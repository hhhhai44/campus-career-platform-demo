package com.hhhhai.ccpd.ai.core.port;

import com.hhhhai.ccpd.ai.core.protocol.AgentMessage;

import java.util.Collections;
import java.util.List;

/**
 * 类的含义：会话记忆存储接口。
 * 类的作用：提供会话历史消息的读写能力，用于多轮对话上下文管理。
 */
public interface MemoryStore {

  MemoryStore NO_OP = new MemoryStore() {
  };

  default List<AgentMessage> load(String sessionId) {
    return Collections.emptyList();
  }

  default void save(String sessionId, List<AgentMessage> newMessages) {
    // no-op
  }
}
