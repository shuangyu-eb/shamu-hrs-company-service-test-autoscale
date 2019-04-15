package shamu.company.common.exception.response;

import java.util.HashMap;
import lombok.Data;
import org.springframework.web.bind.MethodArgumentNotValidException;
import shamu.company.common.exception.AbstractException;

@Data
public class ErrorMessage {

  private String type;
  private String message;
  private HashMap<String, String> errors;

  public ErrorMessage(ErrorType errorType, String message) {
    this.type = errorType.name();
    this.message = message;
  }

  public ErrorMessage(String errorType, String message) {
    this.type = errorType;
    this.message = message;
  }

  public ErrorMessage(MethodArgumentNotValidException e) {
    this.type = ErrorType.JSON_PARSE_ERROR.name();
    this.errors = new HashMap<>();
    e.getBindingResult().getFieldErrors().stream()
        .map((ex) -> this.errors.put(ex.getField(), ex.getDefaultMessage()));
  }

  public ErrorMessage(AbstractException e) {
    this.type = e.getType();
    this.message = e.getMessage();
  }
}
