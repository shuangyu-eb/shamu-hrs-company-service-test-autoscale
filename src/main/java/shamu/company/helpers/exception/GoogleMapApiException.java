package shamu.company.helpers.exception;

public class GoogleMapApiException extends RuntimeException{

  private static final long serialVersionUID = -5967342134911656569L;

  public GoogleMapApiException(final String message) {
    super(message);
  }

  public GoogleMapApiException(final String message, final Throwable e) {
    super(message, e);
  }

}
