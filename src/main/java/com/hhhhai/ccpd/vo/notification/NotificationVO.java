package com.hhhhai.ccpd.vo.notification;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 消息通知视图对象
 */
@Data
public class NotificationVO {

  private Long id;

  /**
   * 通知类型 code：1-评论 2-点赞 3-收藏
   */
  private Integer type;

  /**
   * 通知类型可读名称（与 type 成对返回，前端展示用）
   */
  private String typeDesc;

  /** 同上，别名 typeName 便于前端统一使用 *Name */
  private String typeName;

  /**
   * 业务类型 code：1-帖子 2-资源 3-评论
   */
  private Integer bizType;

  /**
   * 业务类型可读名称
   */
  private String bizTypeDesc;

  /** 同上，别名 bizTypeName */
  private String bizTypeName;

  /**
   * 业务ID，如帖子ID/资源ID/评论ID
   */
  private Long bizId;

  /**
   * 通知标题
   */
  private String title;

  /**
   * 通知内容摘要
   */
  private String content;

  /**
   * 触发行为的用户ID
   */
  private Long fromUserId;

  /**
   * 触发行为的用户昵称（预留，当前为 null）
   */
  private String fromUserName;

  /**
   * 是否已读 code：0-未读 1-已读
   */
  private Integer isRead;

  /**
   * 已读状态可读名称
   */
  private String isReadDesc;

  /** 同上，别名 isReadName */
  private String isReadName;

  /**
   * 创建时间
   */
  private LocalDateTime createTime;
}

