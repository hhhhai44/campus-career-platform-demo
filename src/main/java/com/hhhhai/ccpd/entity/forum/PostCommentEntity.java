package com.hhhhai.ccpd.entity.forum;

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
 * 帖子评论实体（支持二级评论）
 */
@Data
@TableName("post_comment")
public class PostCommentEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long postId;

  /**
   * 根评论ID（一级评论为自身ID，二级评论指向所属一级评论ID）
   */
  private Long rootId;

  /**
   * 父评论ID（回复哪个评论）
   */
  private Long parentId;

  /**
   * 评论用户ID
   */
  private Long fromUserId;

  /**
   * 被回复用户ID
   */
  private Long toUserId;

  /**
   * 评论内容
   */
  private String content;

  /**
   * 点赞数量
   */
  private Integer likeCount;

  /**
   * 状态：正常/已删除/屏蔽
   */
  private ContentStatusEnum status;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;
}








