package shamu.company.common.exception;

public class LiquibaseExecuteFailedException extends AbstractException {

  private static final long serialVersionUID = 7743441635016663852L;

  public LiquibaseExecuteFailedException(final String message, final Throwable throwable) {
    super(message, throwable);
  }
}
