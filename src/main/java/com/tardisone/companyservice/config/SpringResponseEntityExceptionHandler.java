package com.tardisone.companyservice.config;

import com.tardisone.companyservice.exception.EmailException;
import com.tardisone.companyservice.exception.ForbiddenException;
import com.tardisone.companyservice.exception.UnAuthenticatedException;
import com.tardisone.companyservice.response.ErrorMessage;
import com.tardisone.companyservice.response.ErrorType;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


@RestControllerAdvice
public class SpringResponseEntityExceptionHandler {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler({ConversionFailedException.class, MethodArgumentTypeMismatchException.class,
      MissingPathVariableException.class, MissingServletRequestParameterException.class})
  public ErrorMessage handleBadRequestException(Exception exception) {
    return new ErrorMessage(ErrorType.BAD_REQUEST, exception.getMessage());
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(ForbiddenException.class)
  public ErrorMessage handleForbiddenException(ForbiddenException exception) {
    return new ErrorMessage(exception.getType(), exception.getMessage());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ErrorMessage handleMethodArgumentNotValidException(
      MethodArgumentNotValidException exception) {
    return new ErrorMessage(exception);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(EmailException.class)
  public ErrorMessage EmailException(EmailException exception) {
    return new ErrorMessage(exception.getType(), exception.getMessage());
  }

  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(UnAuthenticatedException.class)
  public ErrorMessage handleUnauthenticatedException(UnAuthenticatedException exception) {
    return new ErrorMessage(exception.getType(), exception.getMessage());
  }
}
