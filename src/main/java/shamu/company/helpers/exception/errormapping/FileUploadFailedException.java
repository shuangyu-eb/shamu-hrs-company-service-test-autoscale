package shamu.company.helpers.exception.errormapping;

import me.alidg.errors.annotation.ExceptionMapping;
import org.springframework.http.HttpStatus;

@ExceptionMapping(statusCode = HttpStatus.BAD_REQUEST, errorCode = "file_upload.failed")
public class FileUploadFailedException extends RuntimeException {

  private static final long serialVersionUID = -3595146265390488745L;

  public FileUploadFailedException(final String message) {
    super(message);
  }

  public FileUploadFailedException(final String message, final Throwable e) {
    super(message, e);
  }
}
