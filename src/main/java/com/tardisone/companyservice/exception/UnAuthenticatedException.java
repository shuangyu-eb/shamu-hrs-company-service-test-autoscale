package com.tardisone.companyservice.exception;


import com.tardisone.companyservice.response.ErrorType;

public class UnAuthenticatedException extends AbstractException {

  public UnAuthenticatedException(String message) {
    super(message, ErrorType.UNAUTHENTICATED);
  }
}
