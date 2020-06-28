package shamu.company.scheduler.exception;

public class QuartzException extends RuntimeException {

  private static final long serialVersionUID = -8416214180541670542L;

  public QuartzException(final String message, final Throwable throwable) {
    super(message, throwable);
  }
}
