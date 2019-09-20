package shamu.company.common.exception;

import shamu.company.common.exception.response.ErrorType;

public class TooManyRequestException extends AbstractException {
  public TooManyRequestException(final String message, final Throwable throwable) {
    super(message, ErrorType.TOO_MANY_REQUEST_EXCEPTION, throwable);
  }
}
