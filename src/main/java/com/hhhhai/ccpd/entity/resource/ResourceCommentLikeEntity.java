package com.hhhhai.ccpd.entity.resource;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 资源评论点赞记录
 */
@Data
@TableName("resource_comment_like")
public class ResourceCommentLikeEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long commentId;

  private Long userId;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;
}

