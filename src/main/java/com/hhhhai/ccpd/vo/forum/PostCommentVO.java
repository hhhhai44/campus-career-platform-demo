package com.hhhhai.ccpd.vo.forum;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 帖子评论视图对象（包含二级评论）
 */
@Data
public class PostCommentVO {

  private Long id;

  private Long postId;

  private Long rootId;

  private Long parentId;

  private Long fromUserId;

  private String fromUserName;

  private Long toUserId;

  private String toUserName;

  private String content;

  private Integer likeCount;

  /** 当前用户是否已点赞 */
  private Boolean liked;

  private LocalDateTime createTime;

  /**
   * 二级评论列表
   */
  private List<PostCommentVO> children;
}
