package com.hhhhai.ccpd.dto.forum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 帖子审核/状态更新请求
 */
@Data
public class PostModerationDTO {

  @NotNull(message = "状态不能为空")
  private Integer status;
}

