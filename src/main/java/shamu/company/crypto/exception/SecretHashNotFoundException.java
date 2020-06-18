package shamu.company.crypto.exception;

public class SecretHashNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 493372182593244720L;

  public SecretHashNotFoundException(final String message, final Throwable e) {
    super(message, e);
  }
}
