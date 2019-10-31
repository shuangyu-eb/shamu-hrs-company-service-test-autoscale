package shamu.company.common.exception.response;

public enum ErrorType {

  BAD_REQUEST,

  JSON_PARSE_ERROR,

  RESOURCE_NOT_FOUND,

  FORBIDDEN,

  EMAIL_PROCESS_ERROR,

  VALIDATE_ERROR,

  CONFLICT_ERROR,

  UNAUTHENTICATED,

  ENCODING_ERROR,

  DECODING_ERROR,

  GENERAL_EXCEPTION,

  AWS_UPLOAD_ERROR,

  AWS_GET_URL_EXCEPTION,

  AUTH0_EXCEPTION,

  FILE_VALIDATE_EXCEPTION,

  TOO_MANY_REQUEST_EXCEPTION,

  COMPANY_NAME_CONFLICT,
}
