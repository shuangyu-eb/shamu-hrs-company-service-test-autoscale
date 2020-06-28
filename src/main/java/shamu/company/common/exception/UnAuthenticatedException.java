package shamu.company.common.exception;

public class UnAuthenticatedException extends RuntimeException {

  private static final long serialVersionUID = 8929732219369881698L;

  public UnAuthenticatedException(final String message) {
    super(message);
  }
}
