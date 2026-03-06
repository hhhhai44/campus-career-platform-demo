package com.hhhhai.ccpd.entity.forum;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hhhhai.ccpd.common.enums.CategoryStatusEnum;
import com.hhhhai.ccpd.common.enums.LogicDeleteEnum;
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
   * 分类编码，如 KAOYAN/BAOYAN
   */
  private String code;

  /**
   * 分类描述
   */
  private String description;

  /**
   * 排序字段（越小越靠前）
   */
  private Integer sort;

  /**
   * 状态：0-禁用 1-启用，只返回启用的分类
   */
  private CategoryStatusEnum status;

  /**
   * 逻辑删除
   */
  @TableLogic(value = "0", delval = "1")
  private LogicDeleteEnum deleted;

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







