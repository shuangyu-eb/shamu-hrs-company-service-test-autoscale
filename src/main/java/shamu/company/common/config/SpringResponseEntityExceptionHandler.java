package shamu.company.common.config;

import java.util.List;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import shamu.company.common.exception.EmailException;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.exception.UnAuthenticatedException;
import shamu.company.common.exception.ValidationFailedException;
import shamu.company.common.exception.response.ErrorMessage;
import shamu.company.common.exception.response.ErrorType;

@RestControllerAdvice
public class SpringResponseEntityExceptionHandler {

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(ResourceNotFoundException.class)
  public ErrorMessage handleResourceNotFoundException(final Exception exception) {
    return new ErrorMessage(ErrorType.RESOURCE_NOT_FOUND, exception.getMessage());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler({ConversionFailedException.class, MethodArgumentTypeMismatchException.class,
      MissingPathVariableException.class, MissingServletRequestParameterException.class})
  public ErrorMessage handleBadRequestException(final Exception exception) {
    return new ErrorMessage(ErrorType.BAD_REQUEST, exception.getMessage());
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(ForbiddenException.class)
  public ErrorMessage handleForbiddenException(final ForbiddenException exception) {
    return new ErrorMessage(exception.getType(), exception.getMessage());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ErrorMessage handleMethodArgumentNotValidException(
      final MethodArgumentNotValidException exception) {
    return new ErrorMessage(exception);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(EmailException.class)
  public ErrorMessage handleEmailException(final EmailException exception) {
    return new ErrorMessage(exception.getType(), exception.getMessage());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ValidationFailedException.class)
  public List<ErrorMessage> handleValidationFailedException(
      final ValidationFailedException exception) {
    return exception.getErrorMessages();
  }

  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(UnAuthenticatedException.class)
  public ErrorMessage handleUnauthenticatedException(final UnAuthenticatedException exception) {
    return new ErrorMessage(exception.getType(), exception.getMessage());
  }
}
