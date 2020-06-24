package shamu.company.utils.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.FORBIDDEN, errorCode = "file_format.not_acceptable")
public class FileFormatNotAcceptableException extends RuntimeException {

  private static final long serialVersionUID = 8926642219838105057L;

  public FileFormatNotAcceptableException(final String message) {
    super(message);
  }
}
