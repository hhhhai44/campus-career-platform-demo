package com.hhhhai.ccpd.entity.forum;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 帖子点赞记录实体
 */
@Data
@TableName("post_like")
public class PostLikeEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long postId;

  private Long userId;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;
}







