package com.hhhhai.ccpd.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import java.util.Objects;
import lombok.Getter;

/**
 * 内容状态枚举：正常/禁用（帖子、资源、评论等通用）
 */
@Getter
public enum ContentStatusEnum {

  DISABLED(0, "禁用"),
  NORMAL(1, "正常");

  @EnumValue
  private final Integer code;
  private final String description;

  ContentStatusEnum(Integer code, String description) {
    this.code = code;
    this.description = description;
  }

  public static ContentStatusEnum fromCode(Integer code) {
    if (code == null) {
      return null;
    }
    for (ContentStatusEnum e : values()) {
      if (Objects.equals(e.code, code)) {
        return e;
      }
    }
    return null;
  }

  /** 根据 code 返回可读描述，接口返回时使用 */
  public static String getDescByCode(Integer code) {
    ContentStatusEnum e = fromCode(code);
    return e != null ? e.getDescription() : null;
  }
}
