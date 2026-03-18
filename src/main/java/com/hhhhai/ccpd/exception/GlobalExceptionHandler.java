package com.hhhhai.ccpd.exception;

import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import com.hhhhai.ccpd.common.enums.ErrorCode;
import com.hhhhai.ccpd.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
    UserContext user = UserContextHolder.getUser();
    log.warn(
        "业务异常 请求方式={} 路径={} 用户ID={} 用户名={} 错误码={} 错误信息={}",
        request.getMethod(),
        request.getRequestURI(),
        user == null ? "-" : user.getUserId(),
        user == null ? "匿名用户" : user.getUsername(),
        e.getErrorCode().code(),
        e.getMessage());
    return Result.error(e.getErrorCode().code(), e.getErrorCode().message());
  }

  @ExceptionHandler(Exception.class)
  public Result<?> handleException(Exception e, HttpServletRequest request) {
    UserContext user = UserContextHolder.getUser();
    log.error(
        "系统异常 请求方式={} 路径={} 用户ID={} 用户名={}",
        request.getMethod(),
        request.getRequestURI(),
        user == null ? "-" : user.getUserId(),
        user == null ? "匿名用户" : user.getUsername(),
        e);
    return Result.error(ErrorCode.SYSTEM_ERROR.code(), ErrorCode.SYSTEM_ERROR.message());
  }
}
