package shamu.company.utils;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

public class DateUtil {

  public static final String FULL_MONTH_DAY = "MMMM d";
  public static final String FULL_MONTH_DAY_YEAR = "MMMM d, YYYY";

  public static String formatDateTo(TemporalAccessor date, String pattern) {
    return DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH).format(date);
  }

  public static LocalDate getFirstDayOfMonth(LocalDate date) {
    return LocalDate.of(date.getYear(), date.getMonth(), 1);
  }

  public static Timestamp getFirstDayOfMonth(Timestamp timestamp) {
    return fromLocalDate(getFirstDayOfMonth(fromTimestamp(timestamp)));
  }

  public static Timestamp getFirstDayOfCurrentMonth() {
    LocalDate today = LocalDate.now();
    return fromLocalDate(getFirstDayOfMonth(today));
  }

  public static LocalDate getFirstDayOfYear(LocalDate date) {
    return date.with(firstDayOfYear());
  }

  public static Timestamp getFirstDayOfCurrentYear() {
    LocalDate now = LocalDate.now();
    return fromLocalDate(getFirstDayOfYear(now));
  }

  public static Timestamp getToday() {
    LocalDate now = LocalDate.now();
    return fromLocalDate(now);
  }

  public static LocalDate getDayOfNextYear(LocalDate date) {
    return LocalDate.of(date.getYear() + 1, date.getMonth(), 1);
  }

  public static Timestamp getDayOfNextYear() {
    LocalDate today = LocalDate.now();
    return fromLocalDate(getDayOfNextYear(today));
  }

  public static Timestamp getDayOfNextYear(Timestamp timestamp) {
    return fromLocalDate(getDayOfNextYear(fromTimestamp(timestamp)));
  }

  public static LocalDate fromTimestamp(Timestamp timestamp) {
    return timestamp.toLocalDateTime().toLocalDate();
  }

  public static Timestamp fromLocalDate(LocalDate date) {
    return Timestamp.valueOf(date.atStartOfDay());
  }

  public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
    return LocalDateTime.ofInstant(timestamp.toInstant(), ZoneId.of("UTC"));
  }
}
