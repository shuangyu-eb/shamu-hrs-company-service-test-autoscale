package shamu.company.common.exception.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.web.bind.MethodArgumentNotValidException;
import shamu.company.common.exception.AbstractException;

@Data
public class ErrorMessage {

  private String type;
  private String message;
  private Map<String, List<String>> errors;


  public ErrorMessage(ErrorType errorType, String message) {
    this.type = errorType.name();
    this.message = message;
  }

  public ErrorMessage(String errorType, String message) {
    this.type = errorType;
    this.message = message;
  }

  public ErrorMessage(MethodArgumentNotValidException methodArgumentNotValidException) {
    this.type = ErrorType.JSON_PARSE_ERROR.name();
    this.errors = new HashMap<>();
    methodArgumentNotValidException.getBindingResult().getFieldErrors()
        .forEach(fieldError -> {
          if (errors.containsKey(fieldError.getField())) {
            List<String> errorMsgList = errors.get(fieldError.getField());
            errorMsgList.add(fieldError.getDefaultMessage());
            errors.put(fieldError.getField(),errorMsgList);
          } else {
            List<String> errorMsgList = new ArrayList<>();
            errorMsgList.add(fieldError.getDefaultMessage());
            errors.put(fieldError.getField(), errorMsgList);
          }
        });
  }

  public ErrorMessage(AbstractException abstractException) {
    this.type = abstractException.getType();
    this.message = abstractException.getMessage();
  }
}
