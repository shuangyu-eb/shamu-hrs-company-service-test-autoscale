package shamu.company.user.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.FORBIDDEN, errorCode = "work_email.duplicated")
public class WorkEmailDuplicatedException extends RuntimeException {

  private static final long serialVersionUID = 8420351374421059371L;

  public WorkEmailDuplicatedException(final String message) {
    super(message);
  }
}
