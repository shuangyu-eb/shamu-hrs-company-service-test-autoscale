package com.tardisone.companyservice.exception;


import com.tardisone.companyservice.response.ErrorType;

public class EmailException extends AbstractException {

  public EmailException(String message) {
    super(message, ErrorType.EMAIL_PROCESS_ERROR);
  }

  public EmailException(String message, Throwable throwable) {
    super(message, ErrorType.EMAIL_PROCESS_ERROR, throwable);
  }
}
