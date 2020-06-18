package shamu.company.crypto.exception;

public class AnnotationNotFoundException extends RuntimeException {

  private static final long serialVersionUID = -8954779651909598698L;

  public AnnotationNotFoundException(final String message) {
    super(message);
  }
}
