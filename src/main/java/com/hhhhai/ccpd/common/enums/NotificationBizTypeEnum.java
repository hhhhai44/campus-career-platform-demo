package com.hhhhai.ccpd.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import java.util.Objects;
import lombok.Getter;

/**
 * 通知关联业务类型枚举：帖子 / 资源 / 评论
 */
@Getter
public enum NotificationBizTypeEnum {

  POST(1, "帖子"),
  RESOURCE(2, "资源"),
  COMMENT(3, "评论");

  @EnumValue
  private final Integer code;
  private final String description;

  NotificationBizTypeEnum(Integer code, String description) {
    this.code = code;
    this.description = description;
  }

  public static NotificationBizTypeEnum fromCode(Integer code) {
    if (code == null) {
      return null;
    }
    for (NotificationBizTypeEnum e : values()) {
      if (Objects.equals(e.code, code)) {
        return e;
      }
    }
    return null;
  }

  /** 根据 code 返回可读描述，接口返回时使用 */
  public static String getDescByCode(Integer code) {
    NotificationBizTypeEnum e = fromCode(code);
    return e != null ? e.getDescription() : null;
  }
}
