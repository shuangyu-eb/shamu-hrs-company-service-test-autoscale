package shamu.company.common.validation.validator;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import shamu.company.common.validation.constraints.YesterdayOrLater;

public class YesterdayOrLaterValidator implements ConstraintValidator<YesterdayOrLater, Timestamp> {


  @Override
  public void initialize(final YesterdayOrLater constraintAnnotation) {

  }

  @Override
  public boolean isValid(final Timestamp value, final ConstraintValidatorContext context) {
    if (value != null) {
      final LocalDateTime nativeTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)
          .withNano(0);
      final LocalDateTime targetTime = value.toLocalDateTime().withHour(0).withMinute(0)
          .withSecond(0).withNano(0);

      final Timestamp theTimestampOfYesterday = Timestamp.valueOf(nativeTime.minusDays(1));
      final Timestamp targetTimestamp = Timestamp.valueOf(targetTime);
      return theTimestampOfYesterday.getTime() <= targetTimestamp.getTime();
    }

    return true;
  }

}
