package shamu.company.common.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.FORBIDDEN, errorCode = "email.already_verified")
public class EmailAlreadyVerifiedException extends RuntimeException {

  private static final long serialVersionUID = 5520346560119347563L;

  public EmailAlreadyVerifiedException(final String message) {
    super(message);
  }
}
