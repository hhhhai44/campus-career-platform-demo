package com.hhhhai.ccpd.entity.resource;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hhhhai.ccpd.common.enums.ContentStatusEnum;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 资源评论实体（支持二级评论）
 */
@Data
@TableName("resource_comment")
public class ResourceCommentEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long resourceId;

  private Long rootId;

  private Long parentId;

  private Long fromUserId;

  private Long toUserId;

  private String content;

  private Integer likeCount;

  private ContentStatusEnum status;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;
}

