package shamu.company.helpers.auth0.exception;

public class NonUniqueAuth0ResourceException extends RuntimeException {

  private static final long serialVersionUID = 6240273995858397378L;

  public NonUniqueAuth0ResourceException(final String message) {
    super(message);
  }
}
