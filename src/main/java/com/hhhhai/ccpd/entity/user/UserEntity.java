package com.hhhhai.ccpd.entity.user;

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

@Data
@TableName("user")
public class UserEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 主键ID
   */
  @TableId(type = IdType.AUTO)
  private Long id;

  /**
   * 登录用户名
   */
  private String username;

  /**
   * 加密密码
   */
  private String password;

  /**
   * 真实姓名
   */
  private String realName;

  /**
   * 学号
   */
  private String studentNo;

  /**
   * 邮箱
   */
  private String email;

  /**
   * 手机号
   */
  private String phone;

  /**
   * 角色：1-学生 2-管理员 3-教师
   */
  private Integer role;

  /**
   * 状态：1-正常 0-禁用
   */
  private Integer status;

  /**
   * 连续登录失败次数
   */
  private Integer loginFailCount;

  /**
   * 最后登录时间
   */
  private LocalDateTime lastLoginTime;

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
