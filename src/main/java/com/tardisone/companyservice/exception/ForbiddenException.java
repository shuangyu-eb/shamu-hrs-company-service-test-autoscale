package com.tardisone.companyservice.exception;


import com.tardisone.companyservice.response.ErrorType;

public class ForbiddenException extends AbstractException {

  public ForbiddenException(String message) {
    super(message, ErrorType.FORBIDDEN);
  }

  public ForbiddenException(String message, Throwable throwable) {
    super(message, ErrorType.FORBIDDEN, throwable);
  }
}
