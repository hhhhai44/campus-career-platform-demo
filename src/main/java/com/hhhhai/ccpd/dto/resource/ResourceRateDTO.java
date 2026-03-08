package com.hhhhai.ccpd.dto.resource;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 资源评分请求参数
 */
@Data
public class ResourceRateDTO {

  /**
   * 资源ID
   */
  @NotNull(message = "资源ID不能为空")
  private Long resourceId;

  /**
   * 评分分值 1-5
   */
  @NotNull(message = "评分分值不能为空")
  @Min(value = 1, message = "评分不能小于1")
  @Max(value = 5, message = "评分不能大于5")
  private Integer score;
}

