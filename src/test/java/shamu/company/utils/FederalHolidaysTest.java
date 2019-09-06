package shamu.company.utils;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

class FederalHolidaysTest {
  final private FederalHolidays federalHolidays = new FederalHolidays();
  private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

  @BeforeEach
  public void init() {
    sdf.setTimeZone(TimeZone.getTimeZone("EST"));
  }

  private String getFederalHoliday(String holidayName, int year) {
    return sdf.format(federalHolidays.dateOf(holidayName, year));
  }

  private void testRecentThreeYearsFederalHolidays(
      String federalHoliday, String dayIn2019, String dayIn2020, String dayIn2021) {
    String federalHoliday2019 = getFederalHoliday(federalHoliday, 2019);
    String federalHoliday2020 = getFederalHoliday(federalHoliday, 2020);
    String federalHoliday2021 = getFederalHoliday(federalHoliday, 2021);
    Assert.assertEquals(dayIn2019, federalHoliday2019);
    Assert.assertEquals(dayIn2020, federalHoliday2020);
    Assert.assertEquals(dayIn2021, federalHoliday2021);
  }

  @Test
  void NewYearsDay() {
    testRecentThreeYearsFederalHolidays(
      "New Year's Day", "2019-01-01", "2020-01-01", "2021-01-01");
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
    testRecentThreeYearsFederalHolidays(
        "Memorial Day", "2019-05-27", "2020-05-25", "2021-05-31");
  }

  @Test
  void IndependenceDay() {
    testRecentThreeYearsFederalHolidays(
        "Independence Day", "2019-07-04", "2020-07-04", "2021-07-04");
  }

  @Test
  void LaborDay() {
    testRecentThreeYearsFederalHolidays(
        "Labor Day", "2019-09-02", "2020-09-07", "2021-09-06");
  }

  @Test
  void ColumbusDay() {
    testRecentThreeYearsFederalHolidays(
        "Columbus Day", "2019-10-14", "2020-10-12", "2021-10-11");
  }

  @Test
  void VeteransDay() {
    testRecentThreeYearsFederalHolidays(
        "Veterans Day", "2019-11-11", "2020-11-11", "2021-11-11");
  }

  @Test
  void ThanksgivingDay() {
    testRecentThreeYearsFederalHolidays(
        "Thanksgiving Day", "2019-11-28", "2020-11-26", "2021-11-25");
  }

  @Test
  void ChristmasDay() {
    testRecentThreeYearsFederalHolidays(
        "Christmas Day", "2019-12-25", "2020-12-25", "2021-12-25");
  }

  @Test
  void invalidDay() {
    String invalidFederalHoliday = "";
    Assert.assertNull(federalHolidays.dateOf(invalidFederalHoliday));
    Assert.assertNull(federalHolidays.dateOf(invalidFederalHoliday, 2022));
  }
}
