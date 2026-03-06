package com.hhhhai.ccpd.common.result;

import lombok.Data;

@Data
public class Result<T> {
  private Integer code;
  private String message;  // 统一用 message
  private T data;

  // 成功 - 无数据
  public static <T> Result<T> success() {
    return success(null);
  }

  // 成功 - 有数据
  public static <T> Result<T> success(T data) {
    return success("success", data);
  }

  // 成功 - 自定义消息
  public static <T> Result<T> success(String message, T data) {
    Result<T> result = new Result<>();
    result.setCode(1);
    result.setMessage(message);
    result.setData(data);
    return result;
  }

  // 失败 - 默认错误码
  public static <T> Result<T> error(String message) {
    return error(0, message);
  }

  // 失败 - 自定义错误码
  public static <T> Result<T> error(Integer code, String message) {
    Result<T> result = new Result<>();
    result.setCode(code);
    result.setMessage(message);
    return result;
  }
}