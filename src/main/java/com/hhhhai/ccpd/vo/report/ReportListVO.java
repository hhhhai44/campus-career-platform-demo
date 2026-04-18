package com.hhhhai.ccpd.vo.report;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReportListVO {

  private Long id;

  private String bizType;

  private String bizTypeDesc;

  private String bizTitle;

  private String bizOwnerName;

  private String reporterName;

  private String reason;

  private Integer status;

  private String statusDesc;

  private LocalDateTime createTime;
}

