package com.hhhhai.ccpd.entity.forum;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 论坛帖子实体
 */
@Data
@TableName("post")
public class PostEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 主键ID
   */
  @TableId(type = IdType.AUTO)
  private Long id;

  /**
   * 标题
   */
  private String title;

  /**
   * 内容（富文本或Markdown）
   */
  private String content;

  /**
   * 所属分类ID
   */
  private Long categoryId;

  /**
   * 作者用户ID
   */
  private Long authorId;

  /**
   * 点赞数量（冗余字段，用于排序，具体实时数从Redis获取）
   */
  private Integer likeCount;

  /**
   * 浏览数量
   */
  private Integer viewCount;

  /**
   * 评论数量
   */
  private Integer commentCount;

  /**
   * 是否置顶：0-否 1-是
   */
  private Integer isTop;

  /**
   * 状态：1-正常 0-已屏蔽
   */
  private Integer status;

  /**
   * 乐观锁版本号
   */
  @Version
  private Integer version;

  /**
   * 逻辑删除：0-未删除 1-已删除
   */
  @TableLogic
  private Integer deleted;

  /**
   * 创建时间
   */
  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  /**
   * 更新时间
   */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
}



