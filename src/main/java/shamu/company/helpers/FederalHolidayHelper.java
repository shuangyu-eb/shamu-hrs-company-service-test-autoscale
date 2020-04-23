package shamu.company.helpers;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static java.time.Month.MAY;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static java.time.Month.SEPTEMBER;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import org.springframework.stereotype.Component;

@Component
public class FederalHolidayHelper {

  public Timestamp timestampOf(final Observance observance, final int year) {
    final LocalDate localDate;
    if (observance.isFixedDate()) {
      localDate = LocalDate.of(year, observance.month, observance.dayOfMonth);
    } else {
      localDate = setNthDayOfWeek(observance, year, observance.dayOfWeek, observance.weekOfMonth);
    }

    final LocalDateTime localDateTime = localDate.atTime(LocalTime.of(12, 0, 0));
    return Timestamp.valueOf(localDateTime);
  }

  public Timestamp timestampOf(final String observanceName, final int year) {
    for (final Observance observance : Observance.values()) {
      if (observance.getName().equals(observanceName)) {
        return timestampOf(observance, year);
      }
    }
    return null;
  }

  public Timestamp timestampOf(final String observanceName) {
    final LocalDate now = LocalDate.now();
    final int currentYear = now.getYear();
    return timestampOf(observanceName, currentYear);
  }

  private LocalDate setNthDayOfWeek(
      final Observance observance, final int year, final int dayOfWeek, final int n) {
    LocalDate localDate = LocalDate.of(year, observance.month, 1);
    int week = 0;
    final int lastDay = localDate.lengthOfMonth();
    // if negative, we loop backwards and count the weeks backwards; if positive, count forward
    final int startDay = n > 0 ? 1 : lastDay;
    final int endDay = n > 0 ? lastDay : 1;
    final int incrementValue = n > 0 ? 1 : -1;
    int dayOfMonth = startDay;
    for (; dayOfMonth != endDay; dayOfMonth += incrementValue) {
      localDate = localDate.withDayOfMonth(dayOfMonth);
      if (localDate.getDayOfWeek().getValue() == dayOfWeek) {
        week += incrementValue;
        if (week == n) {
          break;
        }
      }
    }
    localDate = localDate.withDayOfMonth(dayOfMonth);
    return localDate;
  }

  /**
   * The list of Federal Observances, as per section 6103(a) of title 5 of the United States Code.
   *
   * @see <a href=http://www.law.cornell.edu/uscode/text/5/6103 />
   */
  public enum Observance {

    /** January 1st. */
    NEW_YEARS_DAY("New Year's Day", JANUARY, 1),
    /** Third Monday in January. */
    BIRTHDAY_OF_MARTIN_LUTHER_KING_JR("Martin Luther King Jr. Day", JANUARY, MONDAY, 3),
    /** Third Monday in February. */
    WASHINGTONS_BIRTHDAY("Washington's Birthday", FEBRUARY, MONDAY, 3),
    /** Last Monday in May. */
    MEMORIAL_DAY("Memorial Day", MAY, MONDAY, -1),
    /** July 4th. */
    INDEPENDENCE_DAY("Independence Day", JULY, 4),
    /** First Monday in September. */
    LABOR_DAY("Labor Day", SEPTEMBER, MONDAY, 1),
    /** Second Monday in October. */
    COLUMBUS_DAY("Columbus Day", OCTOBER, MONDAY, 2),
    /** November 11th. */
    VETERANS_DAY("Veterans Day", NOVEMBER, 11),
    /** Fourth Thursday in November. */
    THANKSGIVING_DAY("Thanksgiving Day", NOVEMBER, THURSDAY, 4),
    /** December 25th. */
    CHRISTMAS_DAY("Christmas Day", DECEMBER, 25),
    /** Third Monday in February */
    PRESIDENTS_DAY("President's Day", FEBRUARY, MONDAY, 3);

    private static final int NA = 0;
    private final String name;
    private final int month;
    private final int dayOfMonth;
    private final int dayOfWeek;
    private final int weekOfMonth;

    private Observance(final String name, final Month month, final int dayOfMonth) {
      this.name = name;
      this.month = month.getValue();
      this.dayOfMonth = dayOfMonth;
      dayOfWeek = NA;
      weekOfMonth = NA;
    }

    private Observance(
        final String name, final Month month, final DayOfWeek dayOfWeek, final int weekOfMonth) {
      this.name = name;
      this.month = month.getValue();
      dayOfMonth = NA;
      this.dayOfWeek = dayOfWeek.getValue();
      this.weekOfMonth = weekOfMonth;
    }

    /**
     * Returns true if this observance is a fixed date, e.g. December 25th or January 1st. Non-fixed
     * dates are those that are on a particular day of week and week of the month, e.g. 3rd Thursday
     * in November.
     */
    boolean isFixedDate() {
      return dayOfMonth != NA;
    }

    /** Returns Observance's name */
    private String getName() {
      return name;
    }
  }
}
