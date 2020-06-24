package shamu.company.utils.exception;

public class FileValidateFailedException extends RuntimeException {

  private static final long serialVersionUID = -3172941418819920741L;

  public FileValidateFailedException(final String message) {
    super(message);
  }
}
