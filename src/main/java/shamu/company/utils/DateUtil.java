package shamu.company.utils;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

  public static final String FULL_MONTH_DAY = "MMMM d";
  public static final String FULL_MONTH_DAY_YEAR = "MMMM d, YYYY";

  public static String formatDateTo(final TemporalAccessor date, final String pattern) {
    return DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH).format(date);
  }

  public static LocalDate getFirstDayOfMonth(final LocalDate date) {
    return LocalDate.of(date.getYear(), date.getMonth(), 1);
  }

  public static Timestamp getFirstDayOfMonth(final Timestamp timestamp) {
    return fromLocalDate(getFirstDayOfMonth(fromTimestamp(timestamp)));
  }

  public static Timestamp getFirstDayOfCurrentMonth() {
    final LocalDate today = LocalDate.now();
    return fromLocalDate(getFirstDayOfMonth(today));
  }

  public static LocalDate getFirstDayOfYear(final LocalDate date) {
    return date.with(firstDayOfYear());
  }

  public static Timestamp getFirstDayOfCurrentYear() {
    final LocalDate now = LocalDate.now();
    return fromLocalDate(getFirstDayOfYear(now));
  }

  public static Timestamp getToday() {
    final LocalDate now = LocalDate.now();
    return fromLocalDate(now);
  }

  public static LocalDate getDayOfNextYear(final LocalDate date) {
    return LocalDate.of(date.getYear() + 1, date.getMonth(), 1);
  }

  public static Timestamp getDayOfNextYear() {
    final LocalDate today = LocalDate.now();
    return fromLocalDate(getDayOfNextYear(today));
  }

  public static Timestamp getDayOfNextYear(final Timestamp timestamp) {
    return fromLocalDate(getDayOfNextYear(fromTimestamp(timestamp)));
  }

  public static LocalDate fromTimestamp(final Timestamp timestamp) {
    return toLocalDateTime(timestamp).toLocalDate();
  }

  public static Timestamp fromLocalDate(final LocalDate date) {
    return Timestamp.valueOf(date.atStartOfDay());
  }

  public static LocalDateTime toLocalDateTime(final Timestamp timestamp) {
    return LocalDateTime.ofInstant(timestamp.toInstant(), ZoneOffset.UTC);
  }

  public static LocalDateTime toLocalDateTime(final Date date) {
    return LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
  }

  public static LocalDate toLocalDate(final Date date) {
    return toLocalDateTime(date).toLocalDate();
  }

  //Get current UTC time
  public static LocalDateTime getLocalUtcTime() {
    return LocalDateTime.now(ZoneOffset.UTC);
  }
}
