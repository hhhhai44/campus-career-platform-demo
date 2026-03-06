package com.hhhhai.ccpd.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import java.util.Objects;
import lombok.Getter;

/**
 * 逻辑删除枚举
 */
@Getter
public enum LogicDeleteEnum {

  NOT_DELETED(0, "未删除"),
  DELETED(1, "已删除");

  @EnumValue
  private final Integer code;
  private final String description;

  LogicDeleteEnum(Integer code, String description) {
    this.code = code;
    this.description = description;
  }

  public static LogicDeleteEnum fromCode(Integer code) {
    if (code == null) {
      return null;
    }
    for (LogicDeleteEnum e : values()) {
      if (Objects.equals(e.code, code)) {
        return e;
      }
    }
    return null;
  }

  /** 根据 code 返回可读描述，接口返回时使用 */
  public static String getDescByCode(Integer code) {
    LogicDeleteEnum e = fromCode(code);
    return e != null ? e.getDescription() : null;
  }
}
