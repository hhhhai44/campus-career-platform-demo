package com.hhhhai.ccpd.common.enums;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;

/**
 * 列表时间范围筛选枚举。
 */
@Getter
public enum TimeRangeEnum {
  ALL("all", 0),
  TODAY("today", 1),
  THREE_DAYS("3d", 3),
  SEVEN_DAYS("7d", 7);

  private final String code;
  private final int days;

  TimeRangeEnum(String code, int days) {
    this.code = code;
    this.days = days;
  }

  public static TimeRangeEnum fromCode(String code) {
    if (code == null || code.isBlank()) {
      return ALL;
    }
    for (TimeRangeEnum item : values()) {
      if (item.code.equalsIgnoreCase(code)) {
        return item;
      }
    }
    return ALL;
  }

  /**
   * 返回筛选起始时间；ALL 返回 null 表示不限制。
   */
  public LocalDateTime resolveStartTime() {
    if (this == ALL) {
      return null;
    }
    LocalDate today = LocalDate.now();
    if (this == TODAY) {
      return today.atStartOfDay();
    }
    return today.minusDays(days - 1L).atStartOfDay();
  }
}