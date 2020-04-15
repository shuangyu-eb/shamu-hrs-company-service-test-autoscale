package shamu.company.common.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import shamu.company.common.validation.validator.YesterdayOrLaterValidator;

import javax.validation.ConstraintValidatorContext;
import java.sql.Timestamp;

public class YesterdayOrLaterValidatorTests {

  private final YesterdayOrLaterValidator yesterdayOrLaterValidator = new YesterdayOrLaterValidator();

  @Test
  void whenValueIsNotNull_thenShouldReturnFalse() {
    final Timestamp timestamp = new Timestamp(999);
    final ConstraintValidatorContext constraintValidatorContext = Mockito.mock(ConstraintValidatorContext.class);
    Assertions.assertFalse(() -> yesterdayOrLaterValidator.isValid(timestamp,constraintValidatorContext));
  }

  @Test
  void whenValueIsNull_thenShouldReturnTrue() {
    final ConstraintValidatorContext constraintValidatorContext = Mockito.mock(ConstraintValidatorContext.class);
    Assertions.assertTrue(() -> yesterdayOrLaterValidator.isValid(null,constraintValidatorContext));
  }
}
