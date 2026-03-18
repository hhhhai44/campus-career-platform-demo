package com.hhhhai.ccpd.ai.core.protocol;

import lombok.Builder;

/**
 * 类的含义：检索引用信息对象。
 * 类的作用：在 RAG 场景中承载来源信息与片段内容，支持回答可追溯。
 */
@Builder
public record AgentCitation(String sourceId, String sourceType, String title, String snippet, String url) {
}
