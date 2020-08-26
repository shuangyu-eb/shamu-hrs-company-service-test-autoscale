package shamu.company.common.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.TOO_MANY_REQUESTS, errorCode = "too_many_requests")
public class TooManyRequestException extends RuntimeException {

  private static final long serialVersionUID = -6406448570913777085L;

  public TooManyRequestException(final String message, final Throwable throwable) {
    super(message, throwable);
  }

  public TooManyRequestException(final String message) {
    super(message);
  }
}
