package org.hhhhai.campuscareerplatformdemo.entity.user.enums;

import lombok.Getter;

@Getter
public enum UserStatus {

  DISABLED(0, "禁用"),
  ENABLED(1, "正常");

  @Getter
  private final int code;
  private final String desc;

  UserStatus(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public boolean canLogin() {
    return this == ENABLED;
  }

  public static UserStatus from(Integer code) {
    for (UserStatus status : values()) {
      if (status.code == code) {
        return status;
      }
    }
    throw new IllegalArgumentException("未知用户状态：" + code);
  }

}

