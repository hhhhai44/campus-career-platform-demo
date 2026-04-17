package com.hhhhai.ccpd.dto.report;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportHandleDTO {

  /**
   * 处理状态：1-已处理 2-已驳回
   */
  @NotNull(message = "处理状态不能为空")
  private Integer status;

  private String handleRemark;
}

