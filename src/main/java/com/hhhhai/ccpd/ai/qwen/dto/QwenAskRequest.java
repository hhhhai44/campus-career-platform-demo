package com.hhhhai.ccpd.ai.qwen.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 千问问答请求。
 *
 * <p>TODO(Agent): 后续可扩展字段：sessionId、history(messages)、知识库开关、检索参数等。</p>
 */
@Data
public class QwenAskRequest {

  @NotBlank(message = "问题不能为空")
  private String question;
}


