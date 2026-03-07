package com.hhhhai.ccpd.vo.resource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 资源列表项视图对象
 */
@Data
public class ResourceListItemVO {

  private Long id;

  private String title;

  private String description;

  private Long categoryId;

  private String categoryName;

  private Long uploaderId;

  private String uploaderName;

  private BigDecimal scoreAvg;

  private Integer scoreCount;

  /** 收藏数量 */
  private Integer favoriteCount;

  /** 评论数量 */
  private Integer commentCount;

  private Integer downloadCount;

  private LocalDateTime createTime;
}
