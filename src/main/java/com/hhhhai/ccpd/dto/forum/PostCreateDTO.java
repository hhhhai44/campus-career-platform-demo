package com.hhhhai.ccpd.dto.forum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发帖请求参数
 */
@Data
public class PostCreateDTO {

  /**
   * 标题
   */
  @NotBlank(message = "标题不能为空")
  private String title;

  /**
   * 内容
   */
  @NotBlank(message = "内容不能为空")
  private String content;

  /**
   * 分类ID
   */
  @NotNull(message = "分类不能为空")
  private Long categoryId;
}







