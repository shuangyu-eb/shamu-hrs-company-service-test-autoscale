package shamu.company.common.exception;

import shamu.company.common.exception.response.ErrorType;

/**
 * @deprecated (new exceptions related module like NotFoundException... are created, this old one wait to be deleted)
 */
@Deprecated
public class OldForbiddenException extends AbstractException {

  private static final long serialVersionUID = -4718613663222995020L;

  public OldForbiddenException(final String message) {
    super(message, ErrorType.FORBIDDEN);
  }

  public OldForbiddenException(final String message, final Throwable throwable) {
    super(message, ErrorType.FORBIDDEN, throwable);
  }

  public OldForbiddenException(final String message, final ErrorType errorType) {
    super(message, errorType);
  }
}
