package shamu.company.common.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.FORBIDDEN, errorCode = "permission.denied")
public class ForbiddenException extends RuntimeException {

  private static final long serialVersionUID = 871372955527061826L;

  public ForbiddenException(final String message) {
    super(message);
  }
}
