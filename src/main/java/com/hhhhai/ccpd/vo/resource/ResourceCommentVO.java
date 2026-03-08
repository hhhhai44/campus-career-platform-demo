package com.hhhhai.ccpd.vo.resource;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 资源评论视图对象（包含二级评论）
 */
@Data
public class ResourceCommentVO {

  private Long id;

  private Long resourceId;

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

  private List<ResourceCommentVO> children;
}

