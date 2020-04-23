package shamu.company.common.exception;

import shamu.company.common.exception.response.ErrorType;

public class ForbiddenException extends AbstractException {

  private static final long serialVersionUID = -4718613663222995020L;

  public ForbiddenException(final String message) {
    super(message, ErrorType.FORBIDDEN);
  }

  public ForbiddenException(final String message, final Throwable throwable) {
    super(message, ErrorType.FORBIDDEN, throwable);
  }

  public ForbiddenException(final String message, final ErrorType errorType) {
    super(message, errorType);
  }
}
