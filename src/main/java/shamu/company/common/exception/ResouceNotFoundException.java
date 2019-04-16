package shamu.company.common.exception;

import shamu.company.common.exception.response.ErrorType;

public class ResouceNotFoundException extends AbstractException {

  public ResouceNotFoundException(String message) {
    super(message, ErrorType.RESOURCE_NOT_FOUND);
  }

  public ResouceNotFoundException(String message, Throwable throwable) {
    super(message, ErrorType.RESOURCE_NOT_FOUND, throwable);
  }

}
