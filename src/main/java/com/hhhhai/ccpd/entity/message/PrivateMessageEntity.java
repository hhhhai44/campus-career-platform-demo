package com.hhhhai.ccpd.entity.message;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hhhhai.ccpd.common.enums.ReadStatusEnum;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户私信实体
 */
@Data
@TableName("private_message")
public class PrivateMessageEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.AUTO)
  private Long id;

  /**
   * 发送方用户ID
   */
  private Long fromUserId;

  /**
   * 接收方用户ID
   */
  private Long toUserId;

  /**
   * 私信内容
   */
  private String content;

  /**
   * 是否撤回：0-否 1-是
   */
  private Integer isRecalled;

  /**
   * 发送方是否删除（单侧隐藏）
   */
  private Integer fromDeleted;

  /**
   * 接收方是否删除（单侧隐藏）
   */
  private Integer toDeleted;

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


