package com.hhhhai.ccpd.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import java.util.Objects;
import lombok.Getter;

/**
 * 通知类型枚举：评论通知 / 点赞通知 / 收藏通知
 */
@Getter
public enum NotificationTypeEnum {

  COMMENT(1, "评论通知"),
  LIKE(2, "点赞通知"),
  FAVORITE(3, "收藏通知");

  @EnumValue
  private final Integer code;
  private final String description;

  NotificationTypeEnum(Integer code, String description) {
    this.code = code;
    this.description = description;
  }

  public static NotificationTypeEnum fromCode(Integer code) {
    if (code == null) {
      return null;
    }
    for (NotificationTypeEnum e : values()) {
      if (Objects.equals(e.code, code)) {
        return e;
      }
    }
    return null;
  }

  /** 根据 code 返回可读描述，接口返回时使用 */
  public static String getDescByCode(Integer code) {
    NotificationTypeEnum e = fromCode(code);
    return e != null ? e.getDescription() : null;
  }
}
