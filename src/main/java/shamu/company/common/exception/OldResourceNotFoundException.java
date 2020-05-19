package shamu.company.common.exception;

import shamu.company.common.exception.response.ErrorType;

/** @deprecated this OldResourceNotFoundException should be replaced by ResourceNotFound */
@Deprecated
public class OldResourceNotFoundException extends AbstractException {

  private static final long serialVersionUID = 2618882143270580984L;

  public OldResourceNotFoundException(final String message) {
    super(message, ErrorType.RESOURCE_NOT_FOUND);
  }
}
