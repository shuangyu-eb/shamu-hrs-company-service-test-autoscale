package shamu.company.employee.exception;

import me.alidg.errors.annotation.ExceptionMapping;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.FORBIDDEN, errorCode = "user.invitation_capability_frozen")
public class UserInvitationCapabilityFrozenException extends RuntimeException {

  public UserInvitationCapabilityFrozenException(final String message) {
    super(message);
  }
}
