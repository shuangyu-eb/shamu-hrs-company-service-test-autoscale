package shamu.company.helpers.auth0.exception;

public class TooManyRequestException extends RuntimeException {

  private static final long serialVersionUID = -6406448570913777085L;

  public TooManyRequestException(final String message, final Throwable throwable) {
    super(message, throwable);
  }
}
