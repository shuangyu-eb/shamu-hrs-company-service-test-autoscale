package shamu.company.utils;


import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TimeOffRequestDatesAbstractUtilTest {

  private int getCurrentYear() {
    return LocalDate.now().getYear();
  }

  private int getLastYear() {
    return LocalDate.now().getYear() - 1;
  }

  private int getNextYear() {
    return LocalDate.now().getYear() + 1;
  }

  private String getAbstract(List<LocalDate> dates) {
    return TimeOffRequestDatesPreviewUtils.generateTimeOffRequestDatesPreview(dates);
  }

  private void assertAbstract(List<LocalDate> dates, String expect) {
    Assertions.assertThat(getAbstract(dates)).isEqualTo(expect);
  }

  @Test
  void whenRequestOneDayInCurrentYear() {
    int currentYear = getCurrentYear();
    List<LocalDate> dates = Arrays.asList(LocalDate.of(currentYear, 10, 2));
    assertAbstract(dates, "Oct 2");
  }

  @Test
  void whenRequestOneDayNotInCurrentYear() {
    List<LocalDate> dates = Arrays.asList(LocalDate.of(getNextYear(), 6, 2));
    assertAbstract(dates, "Jun 2, " + getNextYear());
  }

  @Test
  void whenRequestSeparatedDaysInTheSameMonthInCurrentYear() {
    List<LocalDate> dates = Arrays.asList(
        LocalDate.of(getCurrentYear(), 8, 1),
        LocalDate.of(getCurrentYear(), 8, 3));
    assertAbstract(dates, "Aug 1, 3");
  }

  @Test
  void whenRequestSeparatedDaysInTheSameMonthNotInCurrentYear() {
    List<LocalDate> dates = Arrays.asList(
        LocalDate.of(getNextYear(), 8, 1),
        LocalDate.of(getNextYear(), 8, 3));
    assertAbstract(dates, "Aug 1, 3, " + getNextYear());
  }

  @Test
  void whenRequestSeparatedDaysInDifferentMonthsButSameCurrentYear() {
    List<LocalDate> dates = Arrays.asList(
        LocalDate.of(getCurrentYear(), 4, 3),
        LocalDate.of(getCurrentYear(), 4, 5),
        LocalDate.of(getCurrentYear(), 7, 3));
    assertAbstract(dates, "Apr 3, 5, Jul 3");
  }

  @Test
  void whenRequestSeparatedDaysInDifferentMonthsButSameNotCurrentYear() {
    List<LocalDate> dates = Arrays.asList(
        LocalDate.of(getNextYear(), 4, 3),
        LocalDate.of(getNextYear(), 4, 5),
        LocalDate.of(getNextYear(), 7, 3));
    assertAbstract(dates, "Apr 3, 5, Jul 3, " + getNextYear());
  }

  @Test
  void whenRequestSeparatedDaysInDifferentYears() {
    List<LocalDate> dates = Arrays.asList(
        LocalDate.of(getLastYear(), 9, 1),
        LocalDate.of(getCurrentYear(), 1, 1),
        LocalDate.of(getCurrentYear(), 8, 1),
        LocalDate.of(getNextYear(), 2, 1)
    );
    assertAbstract(dates, "Sep 1, " + getLastYear() + ", Jan 1, Aug 1, " + getCurrentYear() + ", Feb 1, " + getNextYear());
  }

  @Test
  void whenRequestContinuousDaysInOneMonthInCurrentYear() {
    List<LocalDate> dates = Arrays.asList(
        LocalDate.of(getCurrentYear(),1,2),
        LocalDate.of(getCurrentYear(),1,3),
        LocalDate.of(getCurrentYear(),1,11),
        LocalDate.of(getCurrentYear(),1,12)
    );
    assertAbstract(dates, "Jan 2 - 3, 11 - 12");
  }

  @Test
  void whenRequestContinuousDaysInOneMonthNotInCurrentYear() {
    List<LocalDate> dates = Arrays.asList(
        LocalDate.of(getNextYear(),1,2),
        LocalDate.of(getNextYear(),1,3),
        LocalDate.of(getNextYear(),1,11),
        LocalDate.of(getNextYear(),1,12)
    );
    assertAbstract(dates, "Jan 2 - 3, 11 - 12, "+getNextYear());
  }

  @Test
  void whenRequestContinuousDaysInDifferentMonthsButInCurrentYear() {
    List<LocalDate> dates = Arrays.asList(
        LocalDate.of(getCurrentYear(),1,1),
        LocalDate.of(getCurrentYear(),1,2),
        LocalDate.of(getCurrentYear(),1,3),
        LocalDate.of(getCurrentYear(),1,31),
        LocalDate.of(getCurrentYear(),2,1),
        LocalDate.of(getCurrentYear(),2,2)
    );
    assertAbstract(dates, "Jan 1 - 3, 31 - Feb 2");
  }

  @Test
  void whenRequestContinuousDaysInDifferentMonthsInSameYearButNotInCurrentYear() {
    List<LocalDate> dates = Arrays.asList(
        LocalDate.of(getNextYear(),1,1),
        LocalDate.of(getNextYear(),1,2),
        LocalDate.of(getNextYear(),1,3),
        LocalDate.of(getNextYear(),1,31),
        LocalDate.of(getNextYear(),2,1),
        LocalDate.of(getNextYear(),2,2)
    );
    assertAbstract(dates, "Jan 1 - 3, 31 - Feb 2, "+getNextYear());
  }

  @Test
  void whenRequestContinuousInDifferentYears() {
    List<LocalDate> dates = Arrays.asList(
        LocalDate.of(getLastYear(),12,30),
        LocalDate.of(getLastYear(),12,31),
        LocalDate.of(getCurrentYear(),1,1),
        LocalDate.of(getCurrentYear(),2,1),
        LocalDate.of(getCurrentYear(),2,2),
        LocalDate.of(getCurrentYear(),2,3),
        LocalDate.of(getCurrentYear(),12,31),
        LocalDate.of(getNextYear(),1,1)
    );
    assertAbstract(dates, "Dec 30, "+getLastYear()+" - Jan 1, Feb 1 - 3, Dec 31, "+getCurrentYear()+" - Jan 1, "+getNextYear());
  }

  @Test
  void whenRequestBothContinuousAndSeparatedDays() {
    List<LocalDate> dates = Arrays.asList(
        LocalDate.of(getLastYear(), 4, 5),
        LocalDate.of(getLastYear(), 5, 1),
        LocalDate.of(getLastYear(),6,30),
        LocalDate.of(getLastYear(), 7, 1),
        LocalDate.of(getLastYear(),12,1),
        LocalDate.of(getLastYear(),12,2),
        LocalDate.of(getLastYear(),12,31),
        LocalDate.of(getCurrentYear(),1,1),
        LocalDate.of(getCurrentYear(), 3, 3),
        LocalDate.of(getCurrentYear(), 4, 2),
        LocalDate.of(getCurrentYear(),8,31),
        LocalDate.of(getCurrentYear(), 9, 1)
    );
    assertAbstract(dates, "Apr 5, May 1, Jun 30 - Jul 1, Dec 1 - 2, 31, "+getLastYear()+" - Jan 1, Mar 3, Apr 2, Aug 31 - Sep 1");
  }
}
