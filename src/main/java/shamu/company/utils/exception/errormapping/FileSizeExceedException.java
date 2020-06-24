package shamu.company.utils.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import me.alidg.errors.annotation.ExposeAsArg;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.FORBIDDEN, errorCode = "file_size.exceed")
public class FileSizeExceedException extends RuntimeException {

  @ExposeAsArg(0)
  private final String maxSize;

  private static final long serialVersionUID = -8240481985075423086L;

  public FileSizeExceedException(final String message, final String maxSize) {
    super(message);
    this.maxSize = maxSize;
  }
}
