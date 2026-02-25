package com.hhhhai.ccpd.dto;


import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class LoginDTO {

  /**
   * 用户名
   */
  private String username;

  /**
   * 密码
   */
  private String password;
//  /**
//   * 验证码
//   */
//  private String captcha;
//
//  /**
//   * 记住密码
//   */
//  private boolean rememberMe;
}
