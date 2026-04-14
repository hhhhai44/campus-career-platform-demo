package com.hhhhai.ccpd.ai.qwen.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 智能问答（DashScope compatible）配置项。
 *
 * <p>说明：敏感信息（API Key）通过环境变量注入，避免提交到仓库。</p>
 * <p>TODO(Agent): 后续可扩展更多配置项（如：温度、超时、重试、上下文窗口、安全策略等）。</p>
 */
@Data
@ConfigurationProperties(prefix = "ccpd.qwen")
public class QwenProperties {

  /**
   * 建议从环境变量注入：DASHSCOPE_API_KEY 或 CCPD_QWEN_API_KEY。
   */
  private String apiKey;

  /**
   * Compatible 模式基础地址（可覆盖）。
   *
   * <p>示例：北京地域默认 https://dashscope.aliyuncs.com/compatible-mode/v1
   * 新加坡地域可配置为 https://dashscope-intl.aliyuncs.com/compatible-mode/v1</p>
   */
  private String baseUrl;

  /**
   * 模型名称（例如 qwen-turbo / qwen-plus）。
   */
  private String model = "qwen-turbo";

  /**
   * 系统提示词，可按社区场景持续调优。
   */
  private String systemPrompt = """
      你是学涯社区的智能问答系统，负责回答学业、求职、生活等问题。

      回答原则：
      - 简洁、清晰、可执行，优先给出用户马上能做的建议。
      - 使用 Markdown 提升可读性（短标题、列表、分点），避免长段落。
      - 语气友好专业，结尾可给一句简短引导。
      """;
}
