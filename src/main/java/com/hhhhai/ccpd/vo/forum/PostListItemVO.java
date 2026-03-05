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

  private Long categoryId;

  private String categoryName;

  private Long authorId;

  private String authorName;

  private Integer likeCount;

  private Integer viewCount;

  private Integer commentCount;

  private LocalDateTime createTime;
}



