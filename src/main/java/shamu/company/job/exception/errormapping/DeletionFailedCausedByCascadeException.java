package shamu.company.job.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import me.alidg.errors.annotation.ExposeAsArg;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.FORBIDDEN, errorCode = "deletion.failed_by_cascade")
public class DeletionFailedCausedByCascadeException extends RuntimeException {

  @ExposeAsArg(0)
  private final String target;

  private static final long serialVersionUID = 2509393646605989047L;

  public DeletionFailedCausedByCascadeException(final String message, final String target) {
    super(message);
    this.target = target;
  }
}
