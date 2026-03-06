package com.hhhhai.ccpd.dto.forum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建帖子评论请求参数
 */
@Data
public class PostCommentCreateDTO {

  /**
   * 帖子ID
   */
  @NotNull(message = "帖子ID不能为空")
  private Long postId;

  /**
   * 评论内容
   */
  @NotBlank(message = "评论内容不能为空")
  private String content;

  /**
   * 根评论ID（一级评论时可为空）
   */
  private Long rootId;

  /**
   * 父评论ID（回复哪个评论，可为空）
   */
  private Long parentId;

  /**
   * 被回复用户ID（可为空）
   */
  private Long toUserId;
}







