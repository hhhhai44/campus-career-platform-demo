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

  /** 帖子状态：1-正常 0-禁用 */
  private Integer status;

  /** 逻辑删除：0-未删除 1-已删除 */
  private Integer deleted;

  private Integer viewCount;

  private Integer commentCount;

  private LocalDateTime createTime;
}
