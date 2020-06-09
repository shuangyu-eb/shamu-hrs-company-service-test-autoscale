package shamu.company.user.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.FORBIDDEN, errorCode = "authentication.failed")
public class AuthenticationFailedException extends RuntimeException {

  private static final long serialVersionUID = -8063317007370637183L;

  public AuthenticationFailedException(final String message) {
    super(message);
  }
}
