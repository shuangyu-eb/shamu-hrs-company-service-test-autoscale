package shamu.company.common.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.FORBIDDEN, errorCode = "zip_code.error")
public class ZipCodeNotExistException extends RuntimeException{

  private static final long serialVersionUID = -4016666350494633484L;

  public ZipCodeNotExistException(final String message) {
    super(message);
  }
}
