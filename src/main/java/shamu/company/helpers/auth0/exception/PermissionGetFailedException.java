package shamu.company.helpers.auth0.exception;

public class PermissionGetFailedException extends RuntimeException {

  private static final long serialVersionUID = -3144631931591556909L;

  public PermissionGetFailedException(final String message, final Throwable e) {
    super(message, e);
  }
}
