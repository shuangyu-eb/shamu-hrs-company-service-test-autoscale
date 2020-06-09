package shamu.company.user.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import me.alidg.errors.annotation.ExposeAsArg;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.BAD_REQUEST, errorCode = "user.not_found_by_email")
public class UserNotFoundByEmailException extends RuntimeException {

  private static final long serialVersionUID = 8774460216044024690L;

  @ExposeAsArg(0)
  private final String email;

  public UserNotFoundByEmailException(final String message, final String email) {
    super(message);
    this.email = email;
  }
}
