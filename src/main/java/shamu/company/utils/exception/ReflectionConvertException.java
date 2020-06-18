package shamu.company.utils.exception;

public class ReflectionConvertException extends RuntimeException {

  private static final long serialVersionUID = 8958914966947804100L;

  public ReflectionConvertException(final String message, final Throwable e) {
    super(message, e);
  }
}
