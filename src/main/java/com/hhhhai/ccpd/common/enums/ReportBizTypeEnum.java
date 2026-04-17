package com.hhhhai.ccpd.common.enums;

import java.util.Objects;
import lombok.Getter;

/**
 * 举报对象类型
 */
@Getter
public enum ReportBizTypeEnum {

  POST("POST", "帖子"),
  RESOURCE("RESOURCE", "资源"),
  POST_COMMENT("POST_COMMENT", "帖子评论"),
  RESOURCE_COMMENT("RESOURCE_COMMENT", "资源评论");

  private final String code;
  private final String description;

  ReportBizTypeEnum(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public static ReportBizTypeEnum fromCode(String code) {
    if (code == null || code.isBlank()) {
      return null;
    }
    for (ReportBizTypeEnum item : values()) {
      if (Objects.equals(item.code, code)) {
        return item;
      }
    }
    return null;
  }

  public static String getDescByCode(String code) {
    ReportBizTypeEnum item = fromCode(code);
    return item == null ? null : item.getDescription();
  }
}

