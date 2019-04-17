package shamu.company.common.exception;

import shamu.company.common.exception.response.ErrorType;

public class AwsUploadException extends AbstractException {

  public AwsUploadException(String message) {
    super(message, ErrorType.AWS_UPLOAD_ERROR);
  }
}
