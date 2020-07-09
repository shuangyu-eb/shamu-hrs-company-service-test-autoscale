package shamu.company.attendance.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.utils.DateUtil;

/** @author mshumaker */
public abstract class TimeEntryUtils {

  TimeEntryUtils() {}

  public static final Comparator<LocalDateEntryDto> compareByStartDate =
      Comparator.comparing(LocalDateEntryDto::getStartTime);

  public static final Comparator<EmployeeTimeLog> compareByLogStartDate =
      Comparator.comparing(EmployeeTimeLog::getStart);

  public static List<LocalDateEntryDto> transformTimeLogsToLocalDate(
      final List<EmployeeTimeLog> allEmployeeEntries, final StaticTimezone timezone) {

    final ArrayList<LocalDateEntryDto> entries = new ArrayList<>();
    for (final EmployeeTimeLog employeeEntry : allEmployeeEntries) {
      final LocalDateTime startTime =
          DateUtil.convertUnixToLocalDateTime(employeeEntry.getStart(), timezone.getName());
      final int duration = employeeEntry.getDurationMin();
      final LocalDateTime endTime = startTime.plusMinutes(duration);
      final LocalDate week =
          DateUtil.getFirstDayOfWeek(employeeEntry.getStart(), timezone.getName());
      final LocalDateEntryDto entry =
          new LocalDateEntryDto(employeeEntry.getId(), startTime, endTime, week, duration);
      entries.addAll(splitLocalDateEntry(entry, new ArrayList<>()));
    }
    return entries;
  }

  public static List<LocalDateEntryDto> splitLocalDateEntry(
      final LocalDateEntryDto entry, final List<LocalDateEntryDto> result) {
    if (entry.getEndTime().toLocalDate().equals(entry.getStartTime().toLocalDate())) {
      result.add(entry);
      return result;
    } else {
      final LocalDateTime dailyEndTime =
          entry.getStartTime().plusDays(1).toLocalDate().atStartOfDay();
      final int min = (int) ChronoUnit.MINUTES.between(entry.getStartTime(), dailyEndTime);
      final LocalDateEntryDto newDailyEntry =
          new LocalDateEntryDto(
              entry.getTimeLogId(), entry.getStartTime(), dailyEndTime, entry.getWeek(), min);
      result.add(newDailyEntry);
      final LocalDate newWeek =
          dailyEndTime.toLocalDate().compareTo(entry.getWeek().plusDays(7)) == 0
              ? entry.getWeek().plusDays(7)
              : entry.getWeek();
      final LocalDateEntryDto nextDailyEntry =
          new LocalDateEntryDto(
              entry.getTimeLogId(),
              dailyEndTime,
              entry.getEndTime(),
              newWeek,
              entry.getDuration() - min);
      return splitLocalDateEntry(nextDailyEntry, result);
    }
  }
}
