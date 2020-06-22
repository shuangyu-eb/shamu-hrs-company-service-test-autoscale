package shamu.company.helpers.exception;

public class GenerateFileFailedException extends RuntimeException {

  private static final long serialVersionUID = 7688807206447487768L;

  public GenerateFileFailedException(final String message, final Throwable e) {
    super(message, e);
  }
}
