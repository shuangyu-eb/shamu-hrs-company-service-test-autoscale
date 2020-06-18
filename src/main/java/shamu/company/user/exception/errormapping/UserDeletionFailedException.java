package shamu.company.user.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.FORBIDDEN, errorCode = "user_deletion.failed")
public class UserDeletionFailedException extends RuntimeException {

  private static final long serialVersionUID = 3487371384737587798L;

  public UserDeletionFailedException(final String message) {
    super(message);
  }
}
