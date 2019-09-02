package shamu.company.common.exception;

import shamu.company.common.exception.response.ErrorType;

public class GeneralAuth0Exception extends AbstractException {

  private static final long serialVersionUID = 8359076967955523616L;

  public GeneralAuth0Exception(final String message, final Throwable throwable) {
    super(message, ErrorType.AUTH0_EXCEPTION, throwable);
  }

  public GeneralAuth0Exception(final String message) {
    super(message);
  }
}
