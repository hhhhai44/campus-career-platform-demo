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
        "BUSINESS_EXCEPTION method={} uri={} userId={} username={} code={} msg={}",
        request.getMethod(),
        request.getRequestURI(),
        user == null ? "-" : user.getUserId(),
        user == null ? "anonymous" : user.getUsername(),
        e.getErrorCode().code(),
        e.getMessage());
    return Result.error(e.getErrorCode().code(), e.getErrorCode().message());
  }

  @ExceptionHandler(Exception.class)
  public Result<?> handleException(Exception e, HttpServletRequest request) {
    UserContext user = UserContextHolder.getUser();
    log.error(
        "SYSTEM_EXCEPTION method={} uri={} userId={} username={}",
        request.getMethod(),
        request.getRequestURI(),
        user == null ? "-" : user.getUserId(),
        user == null ? "anonymous" : user.getUsername(),
        e);
    return Result.error(ErrorCode.SYSTEM_ERROR.code(), ErrorCode.SYSTEM_ERROR.message());
  }
}
