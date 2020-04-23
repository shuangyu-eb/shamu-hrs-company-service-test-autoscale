package shamu.company.common.validation.validator;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import shamu.company.common.validation.constraints.PhoneNumberValidate;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumberValidate, String> {

  private static boolean isValidNumber(final PhoneNumber phone) {
    if (phone == null) {
      return true;
    }
    final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    return phoneNumberUtil.isValidNumber(phone);
  }

  @Override
  public boolean isValid(String phone, ConstraintValidatorContext constraintValidatorContext) {

    phone = phone.replaceAll("[^0-9]", "");
    final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    PhoneNumber phoneNumber = null;

    try {
      phoneNumber = phoneNumberUtil.parse(phone, "US");
    } catch (Exception e) {
      e.getMessage();
    }

    return isValidNumber(phoneNumber);
  }
}
