package shamu.company.helpers.exception;

public class EmailSendFailedException extends RuntimeException {

  private static final long serialVersionUID = 7188537342138230836L;

  public EmailSendFailedException(final String message) {
    super(message);
  }

  public EmailSendFailedException(final String message, final Throwable e) {
    super(message, e);
  }
}
