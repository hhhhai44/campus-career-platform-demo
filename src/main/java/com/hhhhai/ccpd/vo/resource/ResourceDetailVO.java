package com.hhhhai.ccpd.vo.resource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 资源详情视图对象
 */
@Data
public class ResourceDetailVO {

  private Long id;

  private String title;

  private String description;

  private Long categoryId;

  private String categoryName;

  private Long uploaderId;

  private String uploaderName;

  private String fileUrl;

  private String tags;

  private BigDecimal scoreAvg;

  private Integer scoreCount;

  private Integer likeCount;

  private Integer favoriteCount;

  /** 当前用户是否已点赞 */
  private Boolean liked;

  /** 当前用户是否已收藏 */
  private Boolean favorited;

  private Integer downloadCount;

  private LocalDateTime createTime;
}
