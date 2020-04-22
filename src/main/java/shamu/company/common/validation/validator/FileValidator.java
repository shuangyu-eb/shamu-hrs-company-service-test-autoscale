package shamu.company.common.validation.validator;

import java.util.stream.Stream;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.common.validation.constraints.FileValidate;
import shamu.company.utils.FileValidateUtils;
import shamu.company.utils.FileValidateUtils.FileFormat;

public class FileValidator implements ConstraintValidator<FileValidate, MultipartFile> {

  private Long maxSize;

  private FileFormat[] fileFormats;

  @Override
  public void initialize(final FileValidate constraintAnnotation) {
    fileFormats =
        Stream.of(constraintAnnotation.fileFormat())
            .map(FileFormat::valueOf)
            .toArray(FileFormat[]::new);
    maxSize = constraintAnnotation.maxSize();
  }

  @Override
  public boolean isValid(final MultipartFile file, final ConstraintValidatorContext context) {
    if (!file.isEmpty()) {
      FileValidateUtils.validate(file, maxSize, fileFormats);
    }
    return true;
  }
}
