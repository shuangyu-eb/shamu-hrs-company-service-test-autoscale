package shamu.company.user.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.BAD_REQUEST, errorCode = "email.expired")
public class EmailExpiredException extends RuntimeException {

  private static final long serialVersionUID = 6601034487412622365L;

  public EmailExpiredException(final String message) {
    super(message);
  }
}
