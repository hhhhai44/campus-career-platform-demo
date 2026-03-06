package com.hhhhai.ccpd.entity.notification;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hhhhai.ccpd.common.enums.NotificationBizTypeEnum;
import com.hhhhai.ccpd.common.enums.NotificationTypeEnum;
import com.hhhhai.ccpd.common.enums.ReadStatusEnum;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户消息通知实体
 */
@Data
@TableName("notification")
public class NotificationEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 主键ID
   */
  @TableId(type = IdType.AUTO)
  private Long id;

  /**
   * 接收用户ID
   */
  private Long userId;

  /**
   * 触发行为的用户ID
   */
  private Long fromUserId;

  /**
   * 通知类型：评论/点赞/收藏
   */
  private NotificationTypeEnum type;

  /**
   * 业务类型：帖子/资源/评论
   */
  private NotificationBizTypeEnum bizType;

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
   * 是否已读
   */
  private ReadStatusEnum isRead;

  /**
   * 创建时间
   */
  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;
}

