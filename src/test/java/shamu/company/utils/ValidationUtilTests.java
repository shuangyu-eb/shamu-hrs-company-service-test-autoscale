package shamu.company.utils;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import shamu.company.common.exception.ValidationFailedException;

class ValidationUtilTests {

  @Data
  @AllArgsConstructor
  class SomeDto {

    @Email
    String email;
  }

  @Test
  void testValidateSingleObject() {
    final SomeDto someDto = new SomeDto("qweasd");
    Assertions.assertThrows(ValidationFailedException.class, () -> {
      ValidationUtil.validate(someDto);
    });
  }

  @Test
  void testValidateObjects() {
    final List dtos = new ArrayList<>();
    final SomeDto invalidDto = new SomeDto("qweasd");
    final SomeDto validDto = new SomeDto("123@qwe.com");
    dtos.add(invalidDto);
    dtos.add(validDto);

    Assertions.assertThrows(ValidationFailedException.class, () -> {
      ValidationUtil.validate(dtos);
    });

    try {
      ValidationUtil.validate(dtos);
    } catch (final ValidationFailedException e) {
      Assertions.assertEquals(1, e.getErrorMessages().size());
    }
  }
}
