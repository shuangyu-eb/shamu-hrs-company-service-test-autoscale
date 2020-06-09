package shamu.company.utils.exception;

public class TupleConvertException extends RuntimeException {

  private static final long serialVersionUID = 8625068507133584457L;

  public TupleConvertException(String message,final Throwable e) {
    super(message,e);
  }
}
