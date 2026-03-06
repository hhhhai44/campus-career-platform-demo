package com.hhhhai.ccpd.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import java.util.Objects;
import lombok.Getter;

/**
 * 用户角色枚举：学生 / 管理员 / 教师
 */
@Getter
public enum UserRoleEnum {

  UNKNOWN(0, "未知"),
  STUDENT(1, "学生"),
  ADMIN(2, "管理员"),
  TEACHER(3, "教师");

  @EnumValue
  private final Integer code;
  private final String description;

  UserRoleEnum(Integer code, String description) {
    this.code = code;
    this.description = description;
  }

  public boolean isAdmin() {
    return this == ADMIN;
  }

  public static UserRoleEnum fromCode(Integer code) {
    if (code == null) {
      return null;
    }
    for (UserRoleEnum e : values()) {
      if (Objects.equals(e.code, code)) {
        return e;
      }
    }
    return null;
  }

  /** 根据 code 返回可读描述，接口返回时使用 */
  public static String getDescByCode(Integer code) {
    UserRoleEnum e = fromCode(code);
    return e != null ? e.getDescription() : null;
  }
}
