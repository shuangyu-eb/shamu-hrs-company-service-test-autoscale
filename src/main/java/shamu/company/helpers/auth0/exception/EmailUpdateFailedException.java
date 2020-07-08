package shamu.company.helpers.auth0.exception;

public class EmailUpdateFailedException extends RuntimeException {

  private static final long serialVersionUID = 4429440006299449822L;

  public EmailUpdateFailedException(final String message, final Throwable e) {
    super(message, e);
  }
}
