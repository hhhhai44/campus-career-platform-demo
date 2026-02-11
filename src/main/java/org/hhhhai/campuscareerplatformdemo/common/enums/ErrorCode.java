package org.hhhhai.campuscareerplatformdemo.common.enums;

public enum ErrorCode {

  // 通用
  SUCCESS(0, "成功"),
  SYSTEM_ERROR(10000, "系统异常"),
  PARAM_INVALID(10001, "参数不合法"),

  // 认证相关
  USER_NOT_FOUND(20001, "用户不存在"),
  PASSWORD_ERROR(20002, "用户名或密码错误"),
  USER_BANNED(20003, "账号已被禁用"),
  TOKEN_EXPIRED(20004, "登录状态已失效，请重新登录");

  private final int code;
  private final String messageKey;

  ErrorCode(int code, String messageKey) {
    this.code = code;
    this.messageKey = messageKey;
  }

  public int code() {
    return code;
  }

  public String messageKey() {
    return messageKey;
  }
}

