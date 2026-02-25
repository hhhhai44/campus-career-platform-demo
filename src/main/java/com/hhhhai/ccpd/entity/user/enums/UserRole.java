package com.hhhhai.ccpd.entity.user.enums;

import java.util.Objects;
import lombok.Getter;

@Getter
public enum UserRole {

  UNKNOWN(0, "未知"),
  STUDENT(1, "学生"),
  ADMIN(2, "管理员");

  private final Integer code;
  private final String desc;

  UserRole(Integer code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static UserRole from(Integer code) {
    for (UserRole role : values()) {
      if (Objects.equals(role.code, code)) {
        return role;
      }
    }
    throw new IllegalArgumentException("未知用户状态：" + code);
  }

  /**
   * 是否后台角色
   */
  public boolean isAdmin() {
    return this == ADMIN;
  }
}

