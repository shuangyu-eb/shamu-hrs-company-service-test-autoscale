package shamu.company.common.exception;

import shamu.company.common.exception.response.ErrorType;

public class QuartzException extends AbstractException {

  public QuartzException(String message) {
    super(message, ErrorType.QUARTZ_SCHEDULE_ERROR);
  }
}
