package com.hhhhai.ccpd.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import java.util.Objects;
import lombok.Getter;

/**
 * 已读状态枚举（通知、消息等）
 */
@Getter
public enum ReadStatusEnum {

  UNREAD(0, "未读"),
  READ(1, "已读");

  @EnumValue
  private final Integer code;
  private final String description;

  ReadStatusEnum(Integer code, String description) {
    this.code = code;
    this.description = description;
  }

  public static ReadStatusEnum fromCode(Integer code) {
    if (code == null) {
      return null;
    }
    for (ReadStatusEnum e : values()) {
      if (Objects.equals(e.code, code)) {
        return e;
      }
    }
    return null;
  }

  /** 根据 code 返回可读描述，接口返回时使用 */
  public static String getDescByCode(Integer code) {
    ReadStatusEnum e = fromCode(code);
    return e != null ? e.getDescription() : null;
  }
}
