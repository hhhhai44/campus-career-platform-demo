package com.hhhhai.ccpd.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import java.util.Objects;
import lombok.Getter;

/**
 * 分类状态枚举：启用/禁用（帖子分类、资源分类等）
 */
@Getter
public enum CategoryStatusEnum {

  DISABLED(0, "禁用"),
  ENABLED(1, "启用");

  @EnumValue
  private final Integer code;
  private final String desc;

  CategoryStatusEnum(Integer code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static CategoryStatusEnum fromCode(Integer code) {
    if (code == null) return null;
    for (CategoryStatusEnum e : values()) {
      if (Objects.equals(e.code, code)) return e;
    }
    return null;
  }

  /** 根据 code 返回可读描述，接口返回时使用 */
  public static String getDescByCode(Integer code) {
    CategoryStatusEnum e = fromCode(code);
    return e != null ? e.getDesc() : null;
  }
}
