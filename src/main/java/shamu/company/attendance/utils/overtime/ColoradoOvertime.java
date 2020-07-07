package shamu.company.attendance.utils.overtime;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.utils.overtime.object.HourContainer;

/** @author mshumaker */
public class ColoradoOvertime extends FederalOverTimePay {
  private static final Integer DAILY_OVERTIME_LIMIT_COLORADO = 12 * 60;
  private static final Double DAILY_OVERTIME_RATE_COLORADO = 1.5;

  @Override
  public void calculateDailyOvertime(final List<LocalDateEntryDto> myHours) {
    final HashSet<LocalDate> weeks = new HashSet<>();
    for (final LocalDateEntryDto entry : myHours) {
      weeks.add(entry.getWeek());
    }
    for (final LocalDate week : weeks) {
      final List<LocalDateEntryDto> weeklyHours =
          myHours.stream()
              .filter(
                  myHour ->
                      (DAYS.between(week, myHour.getStartTime().toLocalDate()) < 7)
                          && (DAYS.between(week, myHour.getStartTime().toLocalDate()) > -2))
              .collect(Collectors.toList());
      final HourContainer hourlyOTCounterByDay =
          calculateDailyOvertimeByDay(weeklyHours, week, new HourContainer(weeklyHours));
      final HourContainer hourlyOTCounterByShift =
          calculateDailyOvertimeByShift(weeklyHours, new HourContainer(weeklyHours));
      if (hourlyOTCounterByDay.getTotalDailyOt(week)
          > hourlyOTCounterByShift.getTotalDailyOt(week)) {
        calculateDailyOvertimeByDay(weeklyHours, week, otTracker);
      } else {
        calculateDailyOvertimeByShift(weeklyHours, otTracker);
      }
    }
  }

  private HourContainer calculateDailyOvertimeByShift(
      final List<LocalDateEntryDto> myHours, final HourContainer overtimeTracker) {
    final List<LocalDateEntryDto> previousShifts = new ArrayList<>();
    for (final LocalDateEntryDto singleEntry : myHours) {
      final Integer currentShiftMin = calculateLenCurrentShift(singleEntry, previousShifts);
      if (currentShiftMin > DAILY_OVERTIME_LIMIT_COLORADO) {
        final int overtimeMin =
            Math.min(currentShiftMin - DAILY_OVERTIME_LIMIT_COLORADO, singleEntry.getDuration());
        overtimeTracker.addDailyOvertime(overtimeMin, singleEntry, DAILY_OVERTIME_RATE_COLORADO);
      }
      previousShifts.add(0, singleEntry);
    }
    return overtimeTracker;
  }

  private Integer calculateLenCurrentShift(
      final LocalDateEntryDto singleEntry, final List<LocalDateEntryDto> previousShifts) {
    Integer shiftLen = singleEntry.getDuration();
    int totalBreaks = 0;
    LocalDateTime currentStart = singleEntry.getStartTime();
    for (final LocalDateEntryDto previousEntry : previousShifts) {
      totalBreaks += MINUTES.between(previousEntry.getEndTime(), currentStart);
      final int allowedBreaks =
          (int) Math.ceil((double) (shiftLen + previousEntry.getDuration()) / (60 * 4)) * 10;
      if (totalBreaks < allowedBreaks) {
        shiftLen += previousEntry.getDuration();
        currentStart = previousEntry.getStartTime();
      } else {
        return shiftLen;
      }
    }
    return shiftLen;
  }

  private HourContainer calculateDailyOvertimeByDay(
      final List<LocalDateEntryDto> myHours,
      final LocalDate week,
      final HourContainer overtimeTracker) {
    final HashMap<LocalDate, Integer> totalDailyHours = new HashMap<>();
    for (final LocalDateEntryDto singleEntry : myHours) {
      if (singleEntry.getWeek().equals(week)) {
        final LocalDate currentDay = singleEntry.getStartTime().toLocalDate();
        totalDailyHours.putIfAbsent(currentDay, 0);
        totalDailyHours.compute(currentDay, (key, val) -> val + singleEntry.getDuration());
        if (totalDailyHours.get(currentDay) > DAILY_OVERTIME_LIMIT_COLORADO) {
          final int otMin =
              Math.min(
                  totalDailyHours.get(currentDay) - DAILY_OVERTIME_LIMIT_COLORADO,
                  singleEntry.getDuration());
          overtimeTracker.addDailyOvertime(otMin, singleEntry, DAILY_OVERTIME_RATE_COLORADO);
        }
      }
    }
    return overtimeTracker;
  }
}
