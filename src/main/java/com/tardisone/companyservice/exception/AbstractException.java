package com.tardisone.companyservice.exception;


import com.tardisone.companyservice.response.ErrorType;

public abstract class AbstractException extends RuntimeException {

  protected ErrorType type;

  public AbstractException() {
  }

  public AbstractException(String message) {
    super(message);
  }

  public AbstractException(Throwable throwable) {
    super(throwable);
  }

  public AbstractException(String message, Throwable throwable) {
    super(message, throwable);
  }

  public AbstractException(String message, ErrorType type) {
    super(message);
    this.type = type;
  }

  public AbstractException(String message, ErrorType type, Throwable throwable) {
    super(message, throwable);
    this.type = type;
  }

  public String getType() {
    return type.name();
  }

  @Override
  public String getMessage() {
    return super.getMessage();
  }
}
