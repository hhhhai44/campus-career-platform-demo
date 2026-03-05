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
 * 资源评分实体
 */
@Data
@TableName("resource_rating")
public class ResourceRatingEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long resourceId;

  private Long userId;

  /**
   * 评分值 1-5
   */
  private Integer score;

  /**
   * 评分评语
   */
  private String comment;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;
}



