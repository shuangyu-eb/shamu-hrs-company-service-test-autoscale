package shamu.company.common.exception;


import shamu.company.common.exception.response.ErrorType;

public class UnAuthenticatedException extends AbstractException {

  public UnAuthenticatedException(String message) {
    super(message, ErrorType.UNAUTHENTICATED);
  }
}
