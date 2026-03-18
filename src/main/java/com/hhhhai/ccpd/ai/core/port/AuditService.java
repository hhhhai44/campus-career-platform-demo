package com.hhhhai.ccpd.ai.core.port;

import com.hhhhai.ccpd.ai.core.protocol.AgentChatRequest;
import com.hhhhai.ccpd.ai.core.protocol.AgentChatResponse;

/**
 * 类的含义：智能问答审计接口。
 * 类的作用：在调用前后及异常场景记录审计事件，满足治理与合规需求。
 */
public interface AuditService {

  AuditService NO_OP = new AuditService() {
  };

  default void beforeInvoke(AgentChatRequest request) {
    // no-op
  }

  default void afterInvoke(AgentChatRequest request, AgentChatResponse response) {
    // no-op
  }

  default void onError(AgentChatRequest request, Exception ex) {
    // no-op
  }
}
