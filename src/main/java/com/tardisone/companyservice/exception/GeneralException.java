package com.tardisone.companyservice.exception;


import com.tardisone.companyservice.response.ErrorType;

public class GeneralException extends AbstractException {

  public GeneralException(String message) {
    super(message, ErrorType.GENERAL_EXCEPTION);
  }

  public GeneralException(String message, Throwable throwable) {
    super(message, ErrorType.GENERAL_EXCEPTION, throwable);
  }
}
