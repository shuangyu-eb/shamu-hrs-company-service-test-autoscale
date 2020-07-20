package shamu.company.common.exception;

public class CustomLiquibaseException extends AbstractException {

  private static final long serialVersionUID = 7743441635016663852L;

  public CustomLiquibaseException(final String message, final Throwable throwable) {
    super(message, throwable);
  }
}
