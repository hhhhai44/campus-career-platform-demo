package com.hhhhai.ccpd.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserAdminStatusDTO {

  /**
   * 用户状态：1-正常 0-禁用
   */
  @NotNull(message = "用户状态不能为空")
  private Integer status;
}

