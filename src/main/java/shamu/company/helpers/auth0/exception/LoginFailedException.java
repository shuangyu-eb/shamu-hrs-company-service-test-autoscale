package shamu.company.helpers.auth0.exception;

public class LoginFailedException extends RuntimeException {

  private static final long serialVersionUID = 1040422971833978855L;

  public LoginFailedException(final String message, final Throwable e) {
    super(message, e);
  }
}
