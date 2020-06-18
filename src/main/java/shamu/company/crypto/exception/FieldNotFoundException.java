package shamu.company.crypto.exception;

public class FieldNotFoundException extends RuntimeException {

  private static final long serialVersionUID = -6608384606733586619L;

  public FieldNotFoundException(final String message) {
    super(message);
  }
}
