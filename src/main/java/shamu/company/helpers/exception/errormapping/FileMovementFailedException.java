package shamu.company.helpers.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.BAD_REQUEST, errorCode = "file_movement.failed")
public class FileMovementFailedException extends RuntimeException {

  private static final long serialVersionUID = -394975490570228242L;

  public FileMovementFailedException(final String message, final Throwable e) {
    super(message, e);
  }
}
