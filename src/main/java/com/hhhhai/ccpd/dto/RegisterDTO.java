package com.hhhhai.ccpd.dto;

import lombok.Data;

/**
 * 用户注册请求 DTO
 */
@Data
public class RegisterDTO {

  /**
   * 登录用户名
   */
  private String username;

  /**
   * 登录密码
   */
  private String password;

  /**
   * 确认密码
   */
  private String confirmPassword;

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
}


