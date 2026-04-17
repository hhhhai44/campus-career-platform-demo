package com.hhhhai.ccpd.common.enums;

import java.util.Objects;
import lombok.Getter;

/**
 * 举报状态
 */
@Getter
public enum ReportStatusEnum {

  PENDING(0, "待处理"),
  APPROVED(1, "已处理"),
  REJECTED(2, "已驳回");

  private final Integer code;
  private final String description;

  ReportStatusEnum(Integer code, String description) {
	this.code = code;
	this.description = description;
  }

  public static ReportStatusEnum fromCode(Integer code) {
	if (code == null) {
	  return null;
	}
	for (ReportStatusEnum item : values()) {
	  if (Objects.equals(item.code, code)) {
		return item;
	  }
	}
	return null;
  }

  public static String getDescByCode(Integer code) {
	ReportStatusEnum item = fromCode(code);
	return item == null ? null : item.getDescription();
  }
}

