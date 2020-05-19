package shamu.company.common.exception;

import me.alidg.errors.annotation.ExceptionMapping;
import me.alidg.errors.annotation.ExposeAsArg;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.BAD_REQUEST, errorCode = "resource.not_found")
public class NotFoundException extends RuntimeException {

  @ExposeAsArg(0)
  private final String resourceId;

  @ExposeAsArg(1)
  private final String resourceType;

  private static final long serialVersionUID = 42022701314094153L;

  public NotFoundException(String message, String resourceId, String resourceType) {
    super(message);
    this.resourceId = resourceId;
    this.resourceType = resourceType;
  }
}
