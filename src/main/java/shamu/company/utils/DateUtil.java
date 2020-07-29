package shamu.company.utils;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;
import shamu.company.attendance.exception.ParseDateException;

public abstract class DateUtil {

  public static final String FULL_MONTH_DAY = "MMMM d";
  public static final String FULL_MONTH_DAY_YEAR = "MMMM d, YYYY";
  public static final String SIMPLE_MONTH_DAY_YEAR = "MMM d, yyyy";
  public static final String SIMPLE_MONTH_DAY = "MMM d";
  public static final String DAY_YEAR = "d, yyyy";
  public static final String DAY = "d";

  private DateUtil() {}

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

  public static Timestamp getCurrentTime() {
    return Timestamp.valueOf(LocalDateTime.now());
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

  public static Timestamp longToTimestamp(final long date) {
    return new Timestamp(date);
  }

  // Get current UTC time
  public static LocalDateTime getLocalUtcTime() {
    return LocalDateTime.now(ZoneOffset.UTC);
  }

  public static long stringToLong(final String date) {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d, yyyy", Locale.ENGLISH);
    try {
      return new Timestamp(dateFormat.parse(date).getTime()).getTime();
    } catch (final ParseException e) {
      throw new ParseDateException("Unable to parse date.", e);
    }
  }

  public static long getFirstHourOfWeek(final Timestamp timestamp, final String timezone) {

    final LocalDate currentDate = timestamp.toInstant().atZone(ZoneId.of(timezone)).toLocalDate();
    final LocalDateTime firstDatimeOfWeek =
        currentDate.with(previousOrSame(DayOfWeek.SATURDAY)).atStartOfDay();
    return firstDatimeOfWeek.atZone(ZoneId.of(timezone)).toEpochSecond();
  }

  public static LocalDate getFirstDayOfWeek(final Timestamp timestamp, final String timezone) {

    final LocalDate currentDate = timestamp.toInstant().atZone(ZoneId.of(timezone)).toLocalDate();
    return currentDate.with(previousOrSame(DayOfWeek.SATURDAY));
  }

  public static LocalDateTime convertUnixToLocalDateTime(
      final Timestamp timestamp, final String timezone) {
    return timestamp.toInstant().atZone(ZoneId.of(timezone)).toLocalDateTime();
  }
}
