package com.hhhhai.ccpd.vo.forum;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 帖子详情视图对象
 */
@Data
public class PostDetailVO {

  private Long id;

  private String title;

  private String content;

  private Long categoryId;

  private String categoryName;

  private Long authorId;

  private String authorName;

  private Integer likeCount;

  /** 当前用户是否已点赞（需登录，未登录为 false） */
  private Boolean liked;

  private Integer viewCount;

  private Integer commentCount;

  /** 当前用户是否已收藏（需登录，未登录为 false） */
  private Boolean favorited;

  /** 当前登录用户ID（未登录为 null） */
  private Long currentUserId;

  private LocalDateTime createTime;
}



