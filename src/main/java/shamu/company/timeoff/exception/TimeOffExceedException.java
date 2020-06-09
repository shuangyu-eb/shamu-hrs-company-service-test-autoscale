package shamu.company.timeoff.exception;

import me.alidg.errors.annotation.ExceptionMapping;
import me.alidg.errors.annotation.ExposeAsArg;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.FORBIDDEN, errorCode = "time_off.exceed")
public class TimeOffExceedException extends RuntimeException {

  @ExposeAsArg(0)
  private final String maxBalance;

  private static final long serialVersionUID = 9047542436753940609L;

  public TimeOffExceedException(final String message, final String maxBalance) {
    super(message);
    this.maxBalance = maxBalance;
  }
}
