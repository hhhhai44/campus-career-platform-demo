package com.hhhhai.ccpd.ai.qwen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 智能问答请求。
 *
 * <p>TODO(Agent): 后续可扩展字段：sessionId、history(messages)、知识库开关、检索参数、工具路由策略等。</p>
 */
@Data
public class QwenAskRequest {

  @NotBlank(message = "问题不能为空")
  @Size(max = 2000, message = "问题长度不能超过 2000 个字符")
  private String question;
}
