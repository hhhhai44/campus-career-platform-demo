package com.hhhhai.ccpd.exception;

import com.hhhhai.ccpd.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public Result<?> handleBusinessException(BusinessException e) {
    return Result.error(e.getErrorCode().code(), e.getErrorCode().message());
  }

  @ExceptionHandler(Exception.class)
  public Result<?> handleException(Exception e) {
    log.debug("系统异常", e);
    return Result.error(500, "系统异常");
  }
}
