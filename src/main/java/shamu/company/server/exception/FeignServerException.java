package shamu.company.server.exception;

public class FeignServerException extends RuntimeException {

  private static final long serialVersionUID = -1019777349990545567L;

  public FeignServerException(final String message) {
    super(message);
  }
}
