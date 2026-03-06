package com.hhhhai.ccpd.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import java.util.Objects;
import lombok.Getter;

/**
 * 消息类型枚举：系统消息 / 用户私信
 */
@Getter
public enum MessageTypeEnum {

  SYSTEM(1, "系统消息"),
  PRIVATE(2, "用户私信");

  @EnumValue
  private final Integer code;
  private final String desc;

  MessageTypeEnum(Integer code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public static MessageTypeEnum fromCode(Integer code) {
    if (code == null) {
      return null;
    }
    for (MessageTypeEnum e : values()) {
      if (Objects.equals(e.code, code)) {
        return e;
      }
    }
    return null;
  }

  /** 根据 code 返回可读描述，接口返回时使用 */
  public static String getDescByCode(Integer code) {
    MessageTypeEnum e = fromCode(code);
    return e != null ? e.getDesc() : null;
  }
}
