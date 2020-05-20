package shamu.company.common.exception;

import shamu.company.common.exception.response.ErrorType;

public abstract class AbstractException extends RuntimeException {

  private static final long serialVersionUID = 654998915182282943L;
  protected final ErrorType type;

  public AbstractException() {
    type = ErrorType.GENERAL_EXCEPTION;
  }

  public AbstractException(String message) {
    super(message);
    type = ErrorType.GENERAL_EXCEPTION;
  }

  public AbstractException(Throwable throwable) {
    super(throwable);
    type = ErrorType.GENERAL_EXCEPTION;
  }

  public AbstractException(String message, Throwable throwable) {
    super(message, throwable);
    type = ErrorType.GENERAL_EXCEPTION;
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
