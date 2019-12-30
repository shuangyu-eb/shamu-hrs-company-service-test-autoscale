package shamu.company.common.validation.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang.StringUtils;
import shamu.company.common.validation.constraints.SsnValidate;

public class SsnValidator implements ConstraintValidator<SsnValidate, String> {

  @Override
  public void initialize(SsnValidate constraintAnnotation) {

  }

  @Override
  public boolean isValid(String ssn, ConstraintValidatorContext constraintValidatorContext) {
    if (StringUtils.isEmpty(ssn)) {
      return true;
    }

    String regex = "^(?!00)(?!666)(?!9[0-9][0-9])\\d{3}[- ]?(?!00)\\d{2}[- ]?(?!0000)\\d{4}$";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(ssn);
    boolean result = matcher.matches();

    return result;
  }
}
