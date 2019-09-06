package shamu.company.common.exception;

import shamu.company.common.exception.response.ErrorType;

public class FileValidateException extends AbstractException {

  private static final long serialVersionUID = -8911765487840588711L;

  public FileValidateException(final String message) {
    super(message, ErrorType.FILE_VALIDATE_EXCEPTION);
  }

  public FileValidateException(final String message, final Throwable throwable) {
    super(message, ErrorType.FILE_VALIDATE_EXCEPTION, throwable);
  }
}
