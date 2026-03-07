package com.hhhhai.ccpd.common.enums;

public enum ErrorCode {

  // 通用
  SUCCESS(0, "成功"),
  SYSTEM_ERROR(10000, "系统异常"),
  PARAM_INVALID(10001, "参数不合法"),

  // 认证相关
  USER_NOT_FOUND(20001, "用户不存在"),
  PASSWORD_ERROR(20002, "用户名或密码错误"),
  USER_BANNED(20003, "账号已被禁用"),
  TOKEN_EXPIRED(20004, "登录状态已失效，请重新登录"),
  USER_EXIST(20005, "用户已存在"),
  PASSWORD_NOT_MATCH(20006, "两次输入的密码不一致"),
  NOT_LOGIN(20007, "请先登录"),
  PASSWORD_FORMAT_ERROR(20008, "密码格式不正确"),
  LOGIN_INVALID(20009, "登录已失效，请重新登录"),

  // 资源相关
  RESOURCE_NOT_FOUND(30001, "资源不存在"),
  RESOURCE_SELF_RATE_FORBIDDEN(30002, "不能给自己上传的资源评分"),
  RESOURCE_ALREADY_RATED(30003, "您已评分，不能重复评分");

  private final int code;
  private final String message;

  ErrorCode(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int code() {
    return code;
  }

  public String message() {
    return message;
  }
}
