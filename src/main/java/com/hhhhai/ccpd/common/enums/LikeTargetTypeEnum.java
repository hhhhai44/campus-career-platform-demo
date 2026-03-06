package com.hhhhai.ccpd.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import java.util.Objects;
import lombok.Getter;

/**
 * 点赞类型枚举：帖子点赞 / 资源点赞
 */
@Getter
public enum LikeTargetTypeEnum {

  POST(1, "帖子点赞"),
  RESOURCE(2, "资源点赞");

  @EnumValue
  private final Integer code;
  private final String desc;

  LikeTargetTypeEnum(Integer code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static LikeTargetTypeEnum fromCode(Integer code) {
    if (code == null) {
      return null;
    }
    for (LikeTargetTypeEnum e : values()) {
      if (Objects.equals(e.code, code)) {
        return e;
      }
    }
    return null;
  }

  /** 根据 code 返回可读描述，接口返回时使用 */
  public static String getDescByCode(Integer code) {
    LikeTargetTypeEnum e = fromCode(code);
    return e != null ? e.getDesc() : null;
  }
}
