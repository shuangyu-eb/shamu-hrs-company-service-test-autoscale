package shamu.company.common.exception;

import shamu.company.common.exception.response.ErrorType;

public class GeneralException extends AbstractException {

  public GeneralException(String message) {
    super(message, ErrorType.GENERAL_EXCEPTION);
  }

  public GeneralException(String message, Throwable throwable) {
    super(message, ErrorType.GENERAL_EXCEPTION, throwable);
  }
}
