package com.hhhhai.ccpd.entity.resource;

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
 * 资源分类实体
 */
@Data
@TableName("resource_category")
public class ResourceCategoryEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.AUTO)
  private Long id;

  /**
   * 分类名称
   */
  private String name;

  /**
   * 分类编码，如 KAOYAN/BAOYAN/EMPLOY/GOV
   */
  private String code;

  /**
   * 描述
   */
  private String description;

  /**
   * 排序
   */
  private Integer sort;

  /**
   * 状态：0-禁用 1-启用，只返回启用的分类
   */
  private CategoryStatusEnum status;

  @TableLogic(value = "0", delval = "1")
  private LogicDeleteEnum deleted;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
}







