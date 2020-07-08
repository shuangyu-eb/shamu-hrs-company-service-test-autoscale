package shamu.company.helpers.auth0.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.BAD_REQUEST, errorCode = "verification_email_send.failed")
public class VerificationEmailSendFailedException extends RuntimeException {

  private static final long serialVersionUID = 707896559670139993L;

  public VerificationEmailSendFailedException(final String message, final Throwable e) {
    super(message, e);
  }
}
