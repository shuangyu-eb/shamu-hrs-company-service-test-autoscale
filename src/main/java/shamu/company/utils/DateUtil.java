package shamu.company.utils;

import shamu.company.attendance.exception.ParseDateException;

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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

public abstract class DateUtil {

  public static final String FULL_MONTH_DAY = "MMMM d";
  public static final String FULL_MONTH_DAY_YEAR = "MMMM d, YYYY";
  public static final String DAY_OF_WEEK_SIMPLE_MONTH_DAY_YEAR = "EEE MMM dd, yyyy";
  public static final String SIMPLE_MONTH_DAY_YEAR = "MMM d, yyyy";
  public static final String SIMPLE_MONTH_DAY = "MMM d";
  public static final String DAY_YEAR = "d, yyyy";
  public static final String DAY = "d";
  public static final String HOUR_MINUTE = "HH:mm";
  public static final long MS_OF_ONE_HOUR = 60 * 60 * 1000L;
  public static final long MS_OF_ONE_DAY = 24 * MS_OF_ONE_HOUR;

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

  public static Timestamp getDayOfNext24Hours(final Timestamp timestamp) {
    return Timestamp.valueOf(toLocalDateTime(timestamp).plusDays(1));
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

  public static Timestamp toTimestamp(final Date date) {
    return new Timestamp(date.getTime());
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

  public static LocalDate toLocalDateWithTimeZone(
      final Timestamp timestamp, final String timezone) {
    return timestamp.toInstant().atZone(ZoneId.of(timezone)).toLocalDate();
  }

  public static long getFirstHourOfWeek(final Timestamp timestamp, final String timezone) {
    final LocalDate currentDate = toLocalDateWithTimeZone(timestamp, timezone);
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

  public static Calendar getCalendarInstance(final long unixTimeStamp, final String timeZoneName) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(unixTimeStamp);
    final TimeZone timeZone = TimeZone.getTimeZone(timeZoneName);
    calendar.setTimeZone(timeZone);

    return calendar;
  }

  public static String formatCalendarWithTimezone(final Calendar calendar, final String format) {
    final SimpleDateFormat sdf = new SimpleDateFormat(format);
    sdf.setTimeZone(calendar.getTimeZone());

    return sdf.format(calendar.getTimeInMillis());
  }

  public static String formatTimestampWithTimezone(
      final Timestamp timestamp, final String format, final String timeZoneName) {
    final Calendar calendar = getCalendarInstance(timestamp.getTime(), timeZoneName);
    return formatCalendarWithTimezone(calendar, format);
  }
}
