package com.hhhhai.ccpd.ai.qwen.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 千问（DashScope）配置项。
 *
 * <p>说明：这里把敏感信息（API Key）设计为通过环境变量注入，避免提交到仓库。</p>
 * <p>TODO(Qwen): 后续可扩展更多配置项（如：超时、温度 temperature、top_p、最大上下文等）。</p>
 */
@Data
@ConfigurationProperties(prefix = "ccpd.qwen")
public class QwenProperties {

  /**
   * TODO(Qwen): 从环境变量/配置中心注入 DashScope API Key
   */
  private String apiKey;

  /**
   * TODO(Qwen): 如需自定义网关/地域 endpoint，可使用该字段；当前实现中仅预留，不强绑定 SDK 行为。
   */
  private String baseUrl;

  /**
   * TODO(Qwen): 模型名称（例如 qwen-turbo/qwen-plus），当前实现用作选择/记录。
   */
  private String model = "qwen-turbo";

  /**
   * TODO(Qwen): 系统提示词，后续可以和“论坛职涯场景”深度定制（并做成可编辑配置）。
   */
  private String systemPrompt = """
      你是学涯社区的智能问答系统，负责回答学业、求职、生活等问题。

      回答原则：
      - 简洁、清晰、可执行，优先给出用户马上能做的建议。
      - 使用 Markdown 提升可读性（短标题、列表、分点），避免长段落。
      - 语气友好专业，结尾可给一句简短引导。

      重要约束：
      - “问题归纳与理解/问题总结”仅用于内部思考，不要作为可见标题输出。
      - 不要输出“详细回答”“结束语或引导”“参考资源或链接”等模板化标签。
      - 直接面向用户给答案，可按“要点/步骤/建议”自然组织。

      当用户问题是“如何做某件事”时，优先输出：
      1) 核心思路
      2) 可执行步骤
      3) 常见误区与提醒（可选）
      4) 简短下一步建议
      """;
}
