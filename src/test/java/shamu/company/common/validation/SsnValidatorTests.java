package shamu.company.common.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import shamu.company.common.validation.validator.SsnValidator;

import javax.validation.ConstraintValidatorContext;

public class SsnValidatorTests {

  private final SsnValidator ssnValidator = new SsnValidator();

  @Test
  void testIsValid() {
    final ConstraintValidatorContext constraintValidatorContext = Mockito.mock(ConstraintValidatorContext.class);
    Assertions.assertFalse(() -> ssnValidator.isValid("1",constraintValidatorContext));
  }
}
