package shamu.company.server.exception;

public class FeignResourceNotFoundException extends RuntimeException {

  private static final long serialVersionUID = -5797763558704816022L;

  public FeignResourceNotFoundException(final String message) {
    super(message);
  }
}
