package com.hhhhai.ccpd.ai.core.protocol;

import lombok.Builder;

/**
 * 类的含义：智能问答消息对象。
 * 类的作用：表示一次对话消息的角色与内容，作为统一协议层的数据单元。
 */
@Builder
public record AgentMessage(AgentMessageRole role, String content) {
}
