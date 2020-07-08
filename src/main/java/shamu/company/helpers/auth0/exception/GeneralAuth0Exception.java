package shamu.company.helpers.auth0.exception;

public class GeneralAuth0Exception extends RuntimeException {

  private static final long serialVersionUID = 8359076967955523616L;

  public GeneralAuth0Exception(final String message, final Throwable e) {
    super(message, e);
  }
}
