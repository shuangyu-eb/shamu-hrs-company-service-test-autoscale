package shamu.company.common.exception;

import shamu.company.common.exception.response.ErrorType;

public class ForbiddenException extends AbstractException {

  public ForbiddenException(String message) {
    super(message, ErrorType.FORBIDDEN);
  }

  public ForbiddenException(String message, Throwable throwable) {
    super(message, ErrorType.FORBIDDEN, throwable);
  }
}
