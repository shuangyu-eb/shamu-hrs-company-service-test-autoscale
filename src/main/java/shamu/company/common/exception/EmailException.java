package shamu.company.common.exception;


import shamu.company.common.exception.response.ErrorType;

public class EmailException extends AbstractException {

  public EmailException(String message) {
    super(message, ErrorType.EMAIL_PROCESS_ERROR);
  }

  public EmailException(String message, Throwable throwable) {
    super(message, ErrorType.EMAIL_PROCESS_ERROR, throwable);
  }
}
