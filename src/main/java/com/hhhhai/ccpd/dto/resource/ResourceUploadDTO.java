package com.hhhhai.ccpd.dto.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 资源上传请求参数
 */
@Data
public class ResourceUploadDTO {

  @NotBlank(message = "资源标题不能为空")
  private String title;

  /**
   * 文章摘要，可选
   */
  private String description;

  /**
   * 文章正文
   */
  @NotBlank(message = "资源正文不能为空")
  private String content;

  /**
   * 分类ID
   */
  @NotNull(message = "资源分类不能为空")
  private Long categoryId;

  /**
   * 标签，可选，逗号分隔
   */
  private String tags;
}
