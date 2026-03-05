package com.hhhhai.ccpd.entity.forum;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 帖子分类实体
 */
@Data
@TableName("post_category")
public class PostCategoryEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 主键ID
   */
  @TableId(type = IdType.AUTO)
  private Long id;

  /**
   * 分类名称
   */
  private String name;

  /**
   * 分类描述
   */
  private String description;

  /**
   * 排序字段（越小越靠前）
   */
  private Integer sort;

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



