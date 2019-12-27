package shamu.company.common.validation.validator;

import java.util.List;
import java.util.stream.Stream;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.common.validation.constraints.FileValidate;
import shamu.company.utils.FileValidateUtil;
import shamu.company.utils.FileValidateUtil.FileType;

public class FilesValidator implements ConstraintValidator<FileValidate, List<MultipartFile>> {

  private Long maxSize;

  private FileType[] fileTypes;

  @Override
  public void initialize(final FileValidate constraintAnnotation) {
    fileTypes = Stream.of(constraintAnnotation.fileType())
        .map(FileType::valueOf)
        .toArray(FileType[]::new);
    maxSize = constraintAnnotation.maxSize();
  }

  @Override
  public boolean isValid(final List<MultipartFile> files,
      final ConstraintValidatorContext context) {
    files.forEach(file -> {
      if (file != null && !file.isEmpty()) {
        FileValidateUtil.validate(file, maxSize, fileTypes);
      }
    });
    return true;
  }
}
