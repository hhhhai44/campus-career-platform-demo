package com.hhhhai.ccpd.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportCreateDTO {

  @NotBlank(message = "举报对象类型不能为空")
  private String bizType;

  @NotNull(message = "举报对象ID不能为空")
  private Long bizId;

  @NotBlank(message = "举报原因不能为空")
  private String reason;

  private String detail;
}

