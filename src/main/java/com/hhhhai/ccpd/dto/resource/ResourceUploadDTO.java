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

  private String description;

  /**
   * 分类ID
   */
  @NotNull(message = "资源分类不能为空")
  private Long categoryId;

  /**
   * 文件访问URL或存储路径
   */
  @NotBlank(message = "资源地址不能为空")
  private String fileUrl;

  /**
   * 标签，可选，逗号分隔
   */
  private String tags;
}







