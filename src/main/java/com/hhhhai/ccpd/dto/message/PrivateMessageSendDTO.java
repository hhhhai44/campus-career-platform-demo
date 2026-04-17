package com.hhhhai.ccpd.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PrivateMessageSendDTO {

  @NotNull(message = "接收方不能为空")
  private Long toUserId;

  @NotBlank(message = "消息内容不能为空")
  @Size(max = 1000, message = "消息内容不能超过1000字")
  private String content;
}

