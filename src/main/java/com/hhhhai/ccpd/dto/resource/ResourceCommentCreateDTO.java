package com.hhhhai.ccpd.dto.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建资源评论请求参数
 */
@Data
public class ResourceCommentCreateDTO {

  @NotNull(message = "资源ID不能为空")
  private Long resourceId;

  @NotBlank(message = "评论内容不能为空")
  private String content;

  private Long rootId;

  private Long parentId;

  private Long toUserId;
}

