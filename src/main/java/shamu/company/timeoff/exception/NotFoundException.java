package shamu.company.timeoff.exception;

import me.alidg.errors.annotation.ExceptionMapping;
import me.alidg.errors.annotation.ExposeAsArg;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.FORBIDDEN, errorCode = "not_found")
public class NotFoundException extends RuntimeException {

  @ExposeAsArg(0)
  private final String name;

  private static final long serialVersionUID = 48797873435319940L;

  public NotFoundException(final String message, final String name) {
    super(message);
    this.name = name;
  }
}
