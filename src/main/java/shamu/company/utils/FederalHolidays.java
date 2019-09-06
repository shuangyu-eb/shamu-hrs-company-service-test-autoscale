package shamu.company.utils;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.DECEMBER;
import static java.util.Calendar.FEBRUARY;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.JANUARY;
import static java.util.Calendar.JULY;
import static java.util.Calendar.MAY;
import static java.util.Calendar.MONDAY;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.NOVEMBER;
import static java.util.Calendar.OCTOBER;
import static java.util.Calendar.SEPTEMBER;
import static java.util.Calendar.THURSDAY;
import static java.util.Calendar.YEAR;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.springframework.stereotype.Component;

@Component
public class FederalHolidays {

  /**
   * The list of Federal Observances, as per section 6103(a) of title 5 of the United States Code.
   *
   * @see <a href=http://www.law.cornell.edu/uscode/text/5/6103 />
   */
  public enum Observance {

    /**
     * January 1st.
     */
    NEW_YEARS_DAY("New Year's Day", JANUARY, 1),
    /**
     * Third Monday in January.
     */
    BIRTHDAY_OF_MARTIN_LUTHER_KING_JR("Martin Luther King Jr. Day", JANUARY, MONDAY, 3),
    /**
     * Third Monday in February.
     */
    WASHINGTONS_BIRTHDAY("Washington's Birthday", FEBRUARY, MONDAY, 3),
    /**
     * Last Monday in May.
     */
    MEMORIAL_DAY("Memorial Day", MAY, MONDAY, -1),
    /**
     * July 4th.
     */
    INDEPENDENCE_DAY("Independence Day", JULY, 4),
    /**
     * First Monday in September.
     */
    LABOR_DAY("Labor Day", SEPTEMBER, MONDAY, 1),
    /**
     * Second Monday in October.
     */
    COLUMBUS_DAY("Columbus Day", OCTOBER, MONDAY, 2),
    /**
     * November 11th.
     */
    VETERANS_DAY("Veterans Day", NOVEMBER, 11),
    /**
     * Fourth Thursday in November.
     */
    THANKSGIVING_DAY("Thanksgiving Day", NOVEMBER, THURSDAY, 4),
    /**
     * December 25th.
     */
    CHRISTMAS_DAY("Christmas Day", DECEMBER, 25),
    /**
     * Third Monday in February
     */
    PRESIDENTS_DAY("President's Day", FEBRUARY, MONDAY, 3);

    private String name;
    private final int month;
    private final int dayOfMonth;
    private final int dayOfWeek;
    private final int weekOfMonth;
    private static final int NA = 0;

    private Observance(String name, int month, int dayOfMonth) {
      this.name = name;
      this.month = month;
      this.dayOfMonth = dayOfMonth;
      this.dayOfWeek = NA;
      this.weekOfMonth = NA;
    }

    private Observance(String name, int month, int dayOfWeek, int weekOfMonth) {
      this.name = name;
      this.month = month;
      this.dayOfMonth = NA;
      this.dayOfWeek = dayOfWeek;
      this.weekOfMonth = weekOfMonth;
    }

    /**
     * Returns true if this observance is a fixed date, e.g. December 25th or January 1st.
     * Non-fixed dates are those that are on a particular day of week and week of the month,
     * e.g. 3rd Thursday in November.
     */
    boolean isFixedDate() {
      return dayOfMonth != NA;
    }

    /**
     * Returns Observance's name
     */
    private String getName() {
      return this.name;
    }
  }

  public Date dateOf(Observance observance, int year) {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("EST"), Locale.ENGLISH);
    cal.set(YEAR, year);
    cal.set(MONTH, observance.month);
    cal.clear(HOUR);
    if (observance.isFixedDate()) {
      cal.set(DAY_OF_MONTH, observance.dayOfMonth);
    } else {
      setNthDayOfWeek(cal, observance.dayOfWeek, observance.weekOfMonth);
    }
    return cal.getTime();
  }

  public Date dateOf(String observanceName, int year) {
    for (Observance observance : Observance.values()) {
      if (observance.getName().equals(observanceName)) {
        return dateOf(observance, year);
      }
    }
    return null;
  }

  public Date dateOf(String observanceName) {
    int thisYear = Calendar.getInstance().get(YEAR);
    return dateOf(observanceName, thisYear);
  }

  /**
   * Mutates the given calendar;
   * sets the date so that it corresponds to the nth occurrence of the given day of the week.
   *
   * @param cal       - the calendar to set the date for
   * @param dayOfWeek - based on the values of Calendar, e.g. Calendar.MONDAY
   * @param n         - the occurrences of the dayOfWeek to set. Can be a value of 1 to 5
   *                 corresponding to the 1st through 5th
   *                  occurrence. If -1, it means "last" occurrence.
   */
  private void setNthDayOfWeek(Calendar cal, int dayOfWeek, int n) {
    int week = 0;
    int lastDay = cal.getActualMaximum(DAY_OF_MONTH);
    // if negative, we loop backwards and count the weeks backwards; if positive, count forward
    int startDay = n > 0 ? 1 : lastDay;
    int endDay = n > 0 ? lastDay : 1;
    int incrementValue = n > 0 ? 1 : -1;
    for (int day = startDay; day != endDay; day += incrementValue) {
      cal.set(DAY_OF_MONTH, day);
      if (cal.get(DAY_OF_WEEK) == dayOfWeek) {
        week += incrementValue;
        if (week == n) {
          return;
        }
      }
    }
  }
}