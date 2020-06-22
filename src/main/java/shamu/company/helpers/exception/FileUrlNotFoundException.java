package shamu.company.helpers.exception;

public class FileUrlNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 2669439435468453860L;

  public FileUrlNotFoundException(final String message, final Throwable e) {
    super(message, e);
  }
}
