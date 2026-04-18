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
   * 通知类型可读名称（前端展示用）
   */
  private String typeDesc;

  /**
   * 通知标题
   */
  private String title;

  /**
   * 通知内容摘要
   */
  private String content;

  /**
   * 是否已读 code：0-未读 1-已读
   */
  private Integer isRead;


  /**
   * 创建时间
   */
  private LocalDateTime createTime;
}

