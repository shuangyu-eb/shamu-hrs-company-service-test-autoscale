package shamu.company.user.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import me.alidg.errors.annotation.ExposeAsArg;
import org.springframework.http.HttpStatus;

@ExceptionMapping(
    statusCode = HttpStatus.BAD_REQUEST,
    errorCode = "user.not_found_by_invitation_token")
public class UserNotFoundByInvitationTokenException extends RuntimeException {

  @ExposeAsArg(0)
  private final String token;

  private static final long serialVersionUID = 6186437328530008583L;

  public UserNotFoundByInvitationTokenException(final String message, final String token) {
    super(message);
    this.token = token;
  }
}
