package shamu.company.user.exception;

public class Auth0UserNotFoundException extends RuntimeException {

  private static final long serialVersionUID = -6889537208352195912L;

  public Auth0UserNotFoundException(final String message) {
    super(message);
  }
}
