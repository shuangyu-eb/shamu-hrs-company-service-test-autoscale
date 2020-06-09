package shamu.company.utils.exception;

public class SerializeFailedException extends RuntimeException {

  private static final long serialVersionUID = 1980254687994046682L;

  public SerializeFailedException(final String message) {
    super(message);
  }
}
