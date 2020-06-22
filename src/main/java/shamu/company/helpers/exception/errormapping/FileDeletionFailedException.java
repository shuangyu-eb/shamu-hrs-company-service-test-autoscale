package shamu.company.helpers.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.BAD_REQUEST, errorCode = "file_deletion.failed")
public class FileDeletionFailedException extends RuntimeException {

  private static final long serialVersionUID = 6200802840815741334L;

  public FileDeletionFailedException(final String message, final Throwable e) {
    super(message, e);
  }
}
