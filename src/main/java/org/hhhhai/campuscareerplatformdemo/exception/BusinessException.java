package org.hhhhai.campuscareerplatformdemo.exception;

import lombok.Getter;
import org.hhhhai.campuscareerplatformdemo.common.enums.ErrorCode;

@Getter
public class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;

  public BusinessException(ErrorCode errorCode) {
    super(errorCode.messageKey());
    this.errorCode = errorCode;
  }

  public BusinessException(ErrorCode errorCode, Throwable cause) {
    super(errorCode.messageKey(), cause);
    this.errorCode = errorCode;
  }

}

