package shamu.company.common.exception;

import shamu.company.common.exception.response.ErrorType;

public abstract class AbstractException extends RuntimeException {

  protected ErrorType type = ErrorType.GENERAL_EXCEPTION;

  public AbstractException() {}

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
}
