package shamu.company.helpers.auth0.exception;

public class SignUpFailedException extends RuntimeException {

  private static final long serialVersionUID = 1040422971833978855L;

  public SignUpFailedException(final String message, final Throwable e) {
    super(message, e);
  }
}
