package com.hhhhai.ccpd.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import java.util.Objects;
import lombok.Getter;

/**
 * 资源分类枚举：考研/保研/就业/考公（仅作 code 映射用，实际分类以 DB 为准）
 */
@Getter
public enum ResourceCategoryEnum {

  KAOYAN(1, "考研"),
  BAOYAN(2, "保研"),
  JOB(3, "就业"),
  GONGWUYUAN(4, "考公");

  @EnumValue
  private final Integer code;
  private final String desc;

  ResourceCategoryEnum(Integer code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static ResourceCategoryEnum fromCode(Integer code) {
    if (code == null) {
      return null;
    }
    for (ResourceCategoryEnum e : values()) {
      if (Objects.equals(e.code, code)) {
        return e;
      }
    }
    return null;
  }

  /** 根据 code 返回可读描述，接口返回时使用（分类名称以 DB 为准，此方法作兜底） */
  public static String getDescByCode(Integer code) {
    ResourceCategoryEnum e = fromCode(code);
    return e != null ? e.getDesc() : null;
  }
}
