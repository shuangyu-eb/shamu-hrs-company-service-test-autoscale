package shamu.company.utils.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.FORBIDDEN, errorCode = "file_size.too_small")
public class FileSizeTooSmallException extends RuntimeException {

  private static final long serialVersionUID = -5895203578964172935L;

  public FileSizeTooSmallException(final String message) {
    super(message);
  }
}

