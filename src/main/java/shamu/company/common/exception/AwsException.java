package shamu.company.common.exception;

import shamu.company.common.exception.response.ErrorType;

public class AwsException extends AbstractException {

  private static final long serialVersionUID = 8265259881140561813L;

  public AwsException(final String message) {
    super(message, ErrorType.AWS_UPLOAD_ERROR);
  }

  public AwsException(final String message, final Throwable throwable) {
    super(message, ErrorType.AWS_UPLOAD_ERROR, throwable);
  }

  public AwsException(final String message, final ErrorType errorType, final Throwable throwable) {
    super(message, errorType, throwable);
  }
}
