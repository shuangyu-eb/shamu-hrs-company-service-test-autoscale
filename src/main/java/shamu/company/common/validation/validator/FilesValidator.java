package shamu.company.common.validation.validator;

import java.util.List;
import java.util.stream.Stream;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.common.validation.constraints.FileValidate;
import shamu.company.utils.FileValidateUtils;
import shamu.company.utils.FileValidateUtils.FileFormat;

public class FilesValidator implements ConstraintValidator<FileValidate, List<MultipartFile>> {

  private Long maxSize;

  private FileFormat[] fileFormats;

  @Override
  public void initialize(final FileValidate constraintAnnotation) {
    fileFormats = Stream.of(constraintAnnotation.fileFormat())
        .map(FileFormat::valueOf)
        .toArray(FileFormat[]::new);
    maxSize = constraintAnnotation.maxSize();
  }

  @Override
  public boolean isValid(final List<MultipartFile> files,
      final ConstraintValidatorContext context) {
    files.forEach(file -> {
      if (file != null && !file.isEmpty()) {
        FileValidateUtils.validate(file, maxSize, fileFormats);
      }
    });
    return true;
  }
}
