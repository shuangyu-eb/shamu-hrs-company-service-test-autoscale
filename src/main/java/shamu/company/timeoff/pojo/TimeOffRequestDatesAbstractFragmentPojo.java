package shamu.company.timeoff.pojo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
// An entire time off request contains many dates .
// Some days are separated while others are continuous.
// Both a separated day or some continuous dates can be considered as
// one fragment of the whole time off request.
public class TimeOffRequestDatesAbstractFragmentPojo {
  private static final String MONTH_DAY_PATTERN = "MMM d";
  private static final String MONTH_PATTERN = "MMM";
  private LocalDate startDate;
  private LocalDate endDate;

  // The fragment only contains one separated day.
  // The only separated day means both start date and end date for this fragment.
  public TimeOffRequestDatesAbstractFragmentPojo(LocalDate singleDate) {
    this.startDate = singleDate;
  }

  // The fragment contains several continuous dates .
  // We only care about the start date and the end date of the time span.
  public TimeOffRequestDatesAbstractFragmentPojo(LocalDate startDate, LocalDate endDate) {
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public LocalDate getLegalEndDate() {
    return endDate == null ? startDate : endDate;
  }

  public String getStartDateMonth() {
    return this.startDate.format(DateTimeFormatter.ofPattern(MONTH_PATTERN));
  }

  public String getLegalEndDateYear() {
    return Integer.toString(getLegalEndDate().getYear());
  }

  @Override
  public String toString() {
    if (endDate == null) {
      return Integer.toString(startDate.getDayOfMonth());
    }
    // The continuous time off dates distribute in different years.
    if (startDate.getYear() != endDate.getYear()) {
      return startDate.getDayOfMonth()
          + ", "
          + startDate.getYear()
          + " - "
          + endDate.format(DateTimeFormatter.ofPattern(MONTH_DAY_PATTERN));
      // The continuous time off dates distribute in different months.
    } else if (startDate.getMonthValue() != endDate.getMonthValue()) {
      return startDate.getDayOfMonth()
          + " - "
          + endDate.format(DateTimeFormatter.ofPattern(MONTH_DAY_PATTERN));
    } else {
      // The continuous time off dates distribute in one month.
      return startDate.getDayOfMonth() + " - " + endDate.getDayOfMonth();
    }
  }
}