package shamu.company.common.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.INTERNAL_SERVER_ERROR, errorCode = "liquibase.failed")
public class LiquibaseExecuteFailedException extends RuntimeException {

  private static final long serialVersionUID = 7743441635016663852L;

  public LiquibaseExecuteFailedException(final String message, final Throwable throwable) {
    super(message, throwable);
  }
}
