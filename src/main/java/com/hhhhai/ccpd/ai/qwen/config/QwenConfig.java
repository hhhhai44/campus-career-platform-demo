package com.hhhhai.ccpd.ai.qwen.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Qwen 模块配置入口。
 *
 * <p>TODO(Agent): 后续 Agent/知识库/工具调用 等能力可以在此模块下继续扩展（例如新增 Bean：向量检索客户端、会话记忆存储等）。</p>
 */
@Configuration
@EnableConfigurationProperties(QwenProperties.class)
public class QwenConfig {
}


