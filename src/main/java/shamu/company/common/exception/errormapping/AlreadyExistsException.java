package shamu.company.common.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import me.alidg.errors.annotation.ExposeAsArg;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.FORBIDDEN, errorCode = "name.already_exists")
public class AlreadyExistsException extends RuntimeException {

  @ExposeAsArg(0)
  private final String name;

  private static final long serialVersionUID = 42022701314094153L;

  public AlreadyExistsException(final String message, final String name) {
    super(message);
    this.name = name;
  }
}
