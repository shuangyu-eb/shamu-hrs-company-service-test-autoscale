package shamu.company.user.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.FORBIDDEN, errorCode = "password.duplicated")
public class PasswordDuplicatedException extends RuntimeException {

  private static final long serialVersionUID = -349312907375091815L;

  public PasswordDuplicatedException(final String message) {
    super(message);
  }
}
