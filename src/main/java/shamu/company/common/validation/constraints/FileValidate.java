package shamu.company.common.validation.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import shamu.company.common.validation.constraints.FileValidate.List;
import shamu.company.common.validation.validator.FileValidator;
import shamu.company.common.validation.validator.FilesValidator;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {FileValidator.class, FilesValidator.class})
@Repeatable(List.class)
public @interface FileValidate {

  String message() default "File format error!";

  long maxSize();

  String[] fileFormat();

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
  @Retention(RUNTIME)
  @Documented
  @interface List {
    FileValidate[] value();
  }
}
