package com.hhhhai.ccpd.vo.report;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReportListVO {

  private Long id;

  private String bizType;

  private String bizTypeDesc;

  private Long bizId;

  private String bizTitle;

  private Long bizOwnerId;

  private String bizOwnerName;

  private Long reporterId;

  private String reporterName;

  private String reason;

  private Integer status;

  private String statusDesc;

  private Long handlerId;

  private String handlerName;

  private LocalDateTime createTime;

  private LocalDateTime updateTime;
}

