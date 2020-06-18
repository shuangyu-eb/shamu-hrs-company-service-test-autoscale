package shamu.company.utils.database.exception;

public class DatabaseCustomTaskFailedException extends RuntimeException {

  private static final long serialVersionUID = 2887263806879324012L;

  public DatabaseCustomTaskFailedException(final String message, final Throwable e) {
    super(message, e);
  }
}
