package com.hhhhai.ccpd.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import java.util.Objects;
import lombok.Getter;

/**
 * 用户状态枚举：启用 / 禁用
 */
@Getter
public enum UserStatusEnum {

  DISABLED(0, "禁用"),
  ENABLED(1, "正常");

  @EnumValue
  private final Integer code;
  private final String description;

  UserStatusEnum(Integer code, String description) {
    this.code = code;
    this.description = description;
  }

  public boolean canLogin() {
    return this == ENABLED;
  }

  public static UserStatusEnum fromCode(Integer code) {
    if (code == null) {
      return null;
    }
    for (UserStatusEnum e : values()) {
      if (Objects.equals(e.code, code)) {
        return e;
      }
    }
    return null;
  }

  /** 根据 code 返回可读描述，接口返回时使用 */
  public static String getDescByCode(Integer code) {
    UserStatusEnum e = fromCode(code);
    return e != null ? e.getDescription() : null;
  }
}
