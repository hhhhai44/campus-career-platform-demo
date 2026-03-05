package com.hhhhai.ccpd.entity.resource;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 资源实体
 */
@Data
@TableName("resource")
public class ResourceEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @TableId(type = IdType.AUTO)
  private Long id;

  /**
   * 资源标题
   */
  private String title;

  /**
   * 资源描述
   */
  private String description;

  /**
   * 分类ID
   */
  private Long categoryId;

  /**
   * 上传用户ID
   */
  private Long uploaderId;

  /**
   * 文件访问URL或存储路径
   */
  private String fileUrl;

  /**
   * 标签，逗号分隔
   */
  private String tags;

  /**
   * 平均评分
   */
  private BigDecimal scoreAvg;

  /**
   * 评分人数
   */
  private Integer scoreCount;

  /**
   * 点赞数量（预留）
   */
  private Integer likeCount;

  /**
   * 收藏数量（预留）
   */
  private Integer favoriteCount;

  /**
   * 下载次数
   */
  private Integer downloadCount;

  /**
   * 状态：1-正常 0-下架
   */
  private Integer status;

  /**
   * 逻辑删除：0-未删除 1-已删除
   */
  @TableLogic
  private Integer deleted;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;
}



