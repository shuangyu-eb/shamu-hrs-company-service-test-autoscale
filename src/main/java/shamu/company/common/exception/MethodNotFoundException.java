package shamu.company.common.exception;

public class MethodNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 8216690203346340162L;

  public MethodNotFoundException(String message, final Throwable e) {
    super(message,e);
  }
}
