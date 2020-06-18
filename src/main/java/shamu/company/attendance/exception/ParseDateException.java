package shamu.company.attendance.exception;

public class ParseDateException extends RuntimeException {

  private static final long serialVersionUID = -6519001049813739364L;

  public ParseDateException(final String message, final Throwable e) {
    super(message, e);
  }
}
