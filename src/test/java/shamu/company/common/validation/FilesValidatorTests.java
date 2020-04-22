package shamu.company.common.validation;

import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.common.validation.constraints.FileValidate;
import shamu.company.common.validation.validator.FilesValidator;

public class FilesValidatorTests {
  private final FilesValidator filesValidator = new FilesValidator();

  @Test
  void testInitialize() {
    final FileValidate fileValidate = Mockito.mock(FileValidate.class);
    final String[] arr = {"JPEG"};
    Mockito.when(fileValidate.fileFormat()).thenReturn(arr);
    Assertions.assertDoesNotThrow(() -> filesValidator.initialize(fileValidate));
  }

  @Test
  void testIsValid() {
    final ConstraintValidatorContext fileValidate = Mockito.mock(ConstraintValidatorContext.class);
    final List<MultipartFile> files = new ArrayList<>();
    files.add(null);
    Assertions.assertTrue(() -> filesValidator.isValid(files, fileValidate));
  }
}
