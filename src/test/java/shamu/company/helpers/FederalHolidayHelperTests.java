package shamu.company.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FederalHolidayHelperTests {
  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  private final FederalHolidayHelper federalHolidayHelper = new FederalHolidayHelper();

  @BeforeEach
  public void init() {
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  private String getFederalHoliday(final String holidayName, final int year) {
    return sdf.format(federalHolidayHelper.timestampOf(holidayName, year));
  }

  private void testRecentThreeYearsFederalHolidays(
      final String federalHoliday,
      final String dayIn2019,
      final String dayIn2020,
      final String dayIn2021) {
    final String federalHoliday2019 = getFederalHoliday(federalHoliday, 2019);
    final String federalHoliday2020 = getFederalHoliday(federalHoliday, 2020);
    final String federalHoliday2021 = getFederalHoliday(federalHoliday, 2021);
    assertThat(federalHoliday2019).isEqualTo(dayIn2019);
    assertThat(federalHoliday2020).isEqualTo(dayIn2020);
    assertThat(federalHoliday2021).isEqualTo(dayIn2021);
  }

  @Test
  void NewYearsDay() {
    testRecentThreeYearsFederalHolidays("New Year's Day", "2019-01-01", "2020-01-01", "2021-01-01");
  }

  @Test
  void MartinLutherKingJrDay() {
    testRecentThreeYearsFederalHolidays(
        "Martin Luther King Jr. Day", "2019-01-21", "2020-01-20", "2021-01-18");
  }

  @Test
  void WashingtonsBirthday() {
    testRecentThreeYearsFederalHolidays(
        "Washington's Birthday", "2019-02-18", "2020-02-17", "2021-02-15");
  }

  @Test
  void MemorialDay() {
    testRecentThreeYearsFederalHolidays("Memorial Day", "2019-05-27", "2020-05-25", "2021-05-31");
  }

  @Test
  void IndependenceDay() {
    testRecentThreeYearsFederalHolidays(
        "Independence Day", "2019-07-04", "2020-07-04", "2021-07-04");
  }

  @Test
  void LaborDay() {
    testRecentThreeYearsFederalHolidays("Labor Day", "2019-09-02", "2020-09-07", "2021-09-06");
  }

  @Test
  void ColumbusDay() {
    testRecentThreeYearsFederalHolidays("Columbus Day", "2019-10-14", "2020-10-12", "2021-10-11");
  }

  @Test
  void VeteransDay() {
    testRecentThreeYearsFederalHolidays("Veterans Day", "2019-11-11", "2020-11-11", "2021-11-11");
  }

  @Test
  void ThanksgivingDay() {
    testRecentThreeYearsFederalHolidays(
        "Thanksgiving Day", "2019-11-28", "2020-11-26", "2021-11-25");
  }

  @Test
  void ChristmasDay() {
    testRecentThreeYearsFederalHolidays("Christmas Day", "2019-12-25", "2020-12-25", "2021-12-25");
  }

  @Test
  void invalidDay() {
    final String invalidFederalHoliday = "";
    assertThat(federalHolidayHelper.timestampOf(invalidFederalHoliday)).isNull();
    assertThat(federalHolidayHelper.timestampOf(invalidFederalHoliday, 2022)).isNull();
  }
}
