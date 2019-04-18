package shamu.company.common.exception;

import shamu.company.common.exception.response.ErrorType;

public class ResourceNotFoundException extends AbstractException {

  public ResourceNotFoundException(String message) {
    super(message, ErrorType.RESOURCE_NOT_FOUND);
  }
}
