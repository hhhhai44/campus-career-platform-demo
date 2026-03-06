package com.hhhhai.ccpd.vo.forum;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 帖子列表项视图对象
 */
@Data
public class PostListItemVO {

  private Long id;

  private String title;

  /** 列表摘要 */
  private String summary;

  private Long categoryId;

  private String categoryName;

  private Long authorId;

  private String authorName;

  private Integer likeCount;

  /** 收藏数量 */
  private Integer favoriteCount;

  private Integer viewCount;

  private Integer commentCount;

  private LocalDateTime createTime;
}
