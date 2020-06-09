package shamu.company.utils.exception;

public class DeserializeFailedException extends RuntimeException {

  private static final long serialVersionUID = 3372813798702855191L;

  public DeserializeFailedException(final String message) {
    super(message);
  }
}
