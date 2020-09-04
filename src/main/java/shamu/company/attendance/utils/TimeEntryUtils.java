package shamu.company.attendance.utils;

import shamu.company.attendance.dto.BreakTimeLogDto;
import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.dto.OvertimeRuleDto;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.utils.DateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** @author mshumaker */
public abstract class TimeEntryUtils {

  TimeEntryUtils() {}

  public static final Comparator<LocalDateEntryDto> compareByStartDate =
      Comparator.comparing(LocalDateEntryDto::getStartTime);

  public static final Comparator<EmployeeTimeLog> compareByLogStartDate =
      Comparator.comparing(EmployeeTimeLog::getStart);

  public static final Comparator<BreakTimeLogDto> compareByBreakStart =
      Comparator.comparing(BreakTimeLogDto::getBreakStart);

  public static final Comparator<OvertimeRuleDto> compareByOvertimeStart =
      Comparator.comparing(OvertimeRuleDto::getStart);

  public static List<LocalDateEntryDto> transformTimeLogsToLocalDate(
      final List<EmployeeTimeLog> allEmployeeEntries, final StaticTimezone timezone) {

    final ArrayList<LocalDateEntryDto> entries = new ArrayList<>();
    for (final EmployeeTimeLog employeeEntry : allEmployeeEntries) {
      // convert start time from utc time zone to company time zone
      final LocalDateTime startTime =
          DateUtil.convertUnixToLocalDateTime(employeeEntry.getStart(), timezone.getName());
      final int duration = employeeEntry.getDurationMin();
      final LocalDateTime endTime = startTime.plusMinutes(duration);
      // get the start of week closest to the period start time
      final LocalDate week =
          DateUtil.getFirstDayOfWeek(employeeEntry.getStart(), timezone.getName());
      final LocalDateEntryDto entry =
          new LocalDateEntryDto(employeeEntry.getId(), startTime, endTime, week, duration);
      // split entry by day for calculation
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
