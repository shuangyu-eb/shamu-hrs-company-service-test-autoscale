package shamu.company.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import shamu.company.common.validation.validator.YesterdayOrLaterValidator;

public class YesterdayOrLaterValidatorTests {

  private final YesterdayOrLaterValidator yesterdayOrLaterValidator =
      new YesterdayOrLaterValidator();

  @Test
  void whenValueIsNotNull_thenShouldReturnFalse() {
    final Timestamp timestamp = new Timestamp(999);
    final ConstraintValidatorContext constraintValidatorContext =
        Mockito.mock(ConstraintValidatorContext.class);
    assertThat(yesterdayOrLaterValidator.isValid(timestamp, constraintValidatorContext)).isFalse();
  }

  @Test
  void whenValueIsNull_thenShouldReturnTrue() {
    final ConstraintValidatorContext constraintValidatorContext =
        Mockito.mock(ConstraintValidatorContext.class);

    assertThat(yesterdayOrLaterValidator.isValid(null, constraintValidatorContext)).isTrue();
  }
}
