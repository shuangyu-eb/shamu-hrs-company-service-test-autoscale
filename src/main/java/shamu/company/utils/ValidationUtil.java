package shamu.company.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import lombok.Data;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.util.CollectionUtils;
import shamu.company.common.exception.ValidationFailedException;
import shamu.company.common.exception.response.ErrorMessage;
import shamu.company.common.exception.response.ErrorType;

@Data
public class ValidationUtil {

  private static final Validator validator = Validation.byProvider(HibernateValidator.class)
      .configure().failFast(false).buildValidatorFactory().getValidator();

  private ValidationUtil() {

  }

  public static void validate(final Object entity) {
    final Set<ConstraintViolation<Object>> constraintViolations = validator.validate(entity);

    if (!CollectionUtils.isEmpty(constraintViolations)) {
      final Map<String, List<String>> errors = getErrors(constraintViolations);
      final ErrorMessage errorMessage = new ErrorMessage(ErrorType.JSON_PARSE_ERROR.toString(),
          errors);
      throw new ValidationFailedException(errorMessage);
    }
  }

  public static void validate(final List<Object> entities) {
    if (!CollectionUtils.isEmpty(entities)) {
      final List<Set<ConstraintViolation<Object>>> sets = entities.stream()
          .map(validator::validate)
          .collect(Collectors.toList());
      List<ErrorMessage> errorMessages = sets.stream().map(set -> {
        if (!set.isEmpty()) {
          Map<String, List<String>> errors = getErrors(set);
          return new ErrorMessage(ErrorType.JSON_PARSE_ERROR.toString(), errors);
        }
        return null;
      }).collect(Collectors.toList());
      errorMessages = errorMessages.stream().filter(Objects::nonNull).collect(Collectors.toList());
      if (!errorMessages.isEmpty()) {
        throw new ValidationFailedException(errorMessages);
      }
    }
  }

  private static Map<String, List<String>> getErrors(
      final Set<ConstraintViolation<Object>> constraintViolations) {
    final Map<String, List<String>> errors = new HashMap<>();
    constraintViolations.forEach(violation -> {
      final PathImpl path = (PathImpl) violation.getPropertyPath();
      final String fieldName = path.toString();
      final String errorMessage = violation.getMessage();

      if (errors.containsKey(fieldName)) {
        final List<String> errorMsgList = errors.get(fieldName);
        errorMsgList.add(errorMessage);
        errors.put(fieldName, errorMsgList);
      } else {
        final List<String> errorMsgList = new ArrayList<>();
        errorMsgList.add(errorMessage);
        errors.put(fieldName, errorMsgList);
      }
    });
    return errors;
  }
}
