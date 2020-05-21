package shamu.company.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import shamu.company.common.validation.validator.SsnValidator;

public class SsnValidatorTests {

  private final SsnValidator ssnValidator = new SsnValidator();
  private final ConstraintValidatorContext constraintValidatorContext =
      Mockito.mock(ConstraintValidatorContext.class);

  private boolean validateSsn(final String ssn) {
    return ssnValidator.isValid(ssn, constraintValidatorContext);
  }

  @Test
  void whenSsnFormatIsInvalid_thenShouldFailed() {
    final String ssn = "123-123123";
    assertThat(validateSsn(ssn)).isFalse();
  }

  @Test
  void whenSsnIsNonNumeric_thenShouldFailed() {
    final String ssn = "123-ab-1234";
    assertThat(validateSsn(ssn)).isFalse();
  }

  @Test
  void whenTheSecondPartOfSsnNotRangeFrom01To99_thenShouldFailed() {
    final String ssn = "123-1234-1234";
    assertThat(validateSsn(ssn)).isFalse();
  }

  @Test
  void whenTheThirdPartOfSsnNotRangeFrom0001To9999_thenShouldFailed() {
    final String ssn = "123-12-12345";
    assertThat(validateSsn(ssn)).isFalse();
  }

  @Test
  void whenAllZerosInFirstPartOfSsn_thenShouldFailed() {
    final String ssn = "000-12-1234";
    assertThat(validateSsn(ssn)).isFalse();
  }

  @Test
  void whenAllZerosInSecondPartOfSsn_thenShouldFailed() {
    final String ssn = "123-00-1234";
    assertThat(validateSsn(ssn)).isFalse();
  }

  @Test
  void whenAllZerosInThirdPartOfSsn_thenShouldFailed() {
    final String ssn = "123-12-0000";
    assertThat(validateSsn(ssn)).isFalse();
  }

  @Test
  void whenSsnBeginsWith666_thenShouldFailed() {
    final String ssn = "666-12-1234";
    assertThat(validateSsn(ssn)).isFalse();
  }

  @Test
  void whenSsnBeginsWithNumberRangeFrom900To999_thenShouldFailed() {
    for (int i = 900; i < 999; i++) {
      final String ssn = i + "-12-1234";
      assertThat(validateSsn(ssn)).isFalse();
    }
  }

  @Test
  void whenSsnIsValid_thenShouldSuccess() {
    final String ssn = "123-12-1234";
    assertThat(validateSsn(ssn)).isTrue();
  }
}
