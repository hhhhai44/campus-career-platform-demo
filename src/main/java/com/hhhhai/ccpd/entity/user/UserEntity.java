package com.hhhhai.ccpd.entity.user;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.hhhhai.ccpd.common.enums.LogicDeleteEnum;
import com.hhhhai.ccpd.common.enums.UserRoleEnum;
import com.hhhhai.ccpd.common.enums.UserStatusEnum;
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
   * 角色：学生/管理员/教师
   */
  private UserRoleEnum role;

  /**
   * 状态：正常/禁用
   */
  private UserStatusEnum status;

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
