package shamu.company.utils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DateUtilTests {

  @Test
  void testFormatDateTo() {
    LocalDateTime localDateTime = LocalDateTime.now();
    Assertions.assertDoesNotThrow(
        () -> DateUtil.formatDateTo(localDateTime, DateUtil.SIMPLE_MONTH_DAY_YEAR));
  }

  @Test
  void testGetFirstDayOfMonthByLocalDate() {
    LocalDate nowDate = LocalDate.now();
    Assertions.assertDoesNotThrow(() -> DateUtil.getFirstDayOfMonth(nowDate));
  }

  @Test
  void testGetFirstDayOfMonthByTimeStamp() {
    Timestamp timestamp = new Timestamp(999);
    Assertions.assertDoesNotThrow(() -> DateUtil.getFirstDayOfMonth(timestamp));
  }

  @Test
  void testGetFirstDayOfCurrentMonth() {
    Assertions.assertDoesNotThrow(() -> DateUtil.getFirstDayOfCurrentMonth());
  }

  @Test
  void testGetFirstDayOfYear() {
    LocalDate nowDate = LocalDate.now();
    Assertions.assertDoesNotThrow(() -> DateUtil.getFirstDayOfYear(nowDate));
  }

  @Test
  void testGetFirstDayOfCurrentYear() {
    Assertions.assertDoesNotThrow(() -> DateUtil.getFirstDayOfCurrentYear());
  }

  @Test
  void testGetToday() {
    Assertions.assertDoesNotThrow(() -> DateUtil.getToday());
  }

  @Test
  void testGetCurrentTime() {
    Assertions.assertDoesNotThrow(() -> DateUtil.getCurrentTime());
  }

  @Test
  void testGetDayOfNextYearByLocalDate() {
    LocalDate nowDate = LocalDate.now();
    Assertions.assertDoesNotThrow(() -> DateUtil.getDayOfNextYear(nowDate));
  }

  @Test
  void testGetDayOfNextYear() {
    Assertions.assertDoesNotThrow(() -> DateUtil.getDayOfNextYear());
  }

  @Test
  void testGetDayOfNextYearByTimeStamp() {
    Timestamp timestamp = new Timestamp(999);
    Assertions.assertDoesNotThrow(() -> DateUtil.getDayOfNextYear(timestamp));
  }

  @Test
  void testFromTimestamp() {
    Timestamp timestamp = new Timestamp(999);
    Assertions.assertDoesNotThrow(() -> DateUtil.fromTimestamp(timestamp));
  }

  @Test
  void testFromLocalDate() {
    LocalDate nowDate = LocalDate.now();
    Assertions.assertDoesNotThrow(() -> DateUtil.fromLocalDate(nowDate));
  }

  @Test
  void testToLocalDateTimeByTimeStamp() {
    Timestamp timestamp = new Timestamp(999);
    Assertions.assertDoesNotThrow(() -> DateUtil.toLocalDateTime(timestamp));
  }

  @Test
  void testToLocalDateTimeByDate() {
    Date date = new Date();
    Assertions.assertDoesNotThrow(() -> DateUtil.toLocalDateTime(date));
  }

  @Test
  void testToLocalDate() {
    Date date = new Date();
    Assertions.assertDoesNotThrow(() -> DateUtil.toLocalDate(date));
  }

  @Test
  void testGetLocalUtcTime() {
    Assertions.assertDoesNotThrow(() -> DateUtil.getLocalUtcTime());
  }
}
