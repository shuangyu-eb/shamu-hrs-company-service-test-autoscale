package shamu.company.common.exception;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import shamu.company.common.exception.response.ErrorMessage;

@Data
public class ValidationFailedException extends AbstractException {

  private static final long serialVersionUID = -1537392884205075093L;
  private final List<ErrorMessage> errorMessages;

  public ValidationFailedException(final ErrorMessage errorMessages) {
    this.errorMessages = new ArrayList<>();
    this.errorMessages.add(errorMessages);
  }

  public ValidationFailedException(final List<ErrorMessage> errorMessages) {
    this.errorMessages = errorMessages;
  }
}
