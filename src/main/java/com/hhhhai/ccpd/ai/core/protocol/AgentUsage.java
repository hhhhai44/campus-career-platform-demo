package com.hhhhai.ccpd.ai.core.protocol;

import lombok.Builder;

/**
 * 类的含义：模型调用用量信息对象。
 * 类的作用：承载 prompt/completion/total token 统计，便于计费与监控。
 */
@Builder
public record AgentUsage(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
}
