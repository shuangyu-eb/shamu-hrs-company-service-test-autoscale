package shamu.company.attendance.utils.overtime;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import shamu.company.attendance.dto.LocalDateEntryDto;

public class CaliforniaOvertimePay extends FederalOverTimePay {
  private static final Integer DAILY_OVERTIME_LIMIT_EIGHT_HOUR_CALIFORNIA = 8 * 60;
  private static final Integer DAILY_OVERTIME_LIMIT_TWELVE_HOUR_CALIFORNIA = 12 * 60;
  private static final Double DAILY_OVERTIME_RATE_EIGHT_TO_TWELVE_CALIFORNIA = 1.5;
  private static final Double DAILY_OVERTIME_RATE_BEYOND_TWELVE_CALIFORNIA = 2.0;
  private static final Double THE_7TH_CONSECUTIVE_RATE_LIMIT_LESS_EIGHT_HOUR = 1.5;
  private static final Double THE_7TH_CONSECUTIVE_RATE_LIMIT_BEYOND_EIGHT_HOUR = 2.0;

  @Override
  public void calculateDailyOvertime(final List<LocalDateEntryDto> myHours) {
    final Set<String> the7thConsecutiveDayTimeLogId = new HashSet<>();
    final HashSet<LocalDate> weeks = new HashSet<>();
    for (final LocalDateEntryDto singleEntry : myHours) {
      weeks.add(singleEntry.getWeek());
    }
    for (final LocalDate week : weeks) {
      final List<LocalDateEntryDto> weeklyHours =
          myHours.stream()
              .filter(myHour -> myHour.getWeek().equals(week))
              .collect(Collectors.toList());
      calculate7thDayOvertime(weeklyHours, week, the7thConsecutiveDayTimeLogId);
    }

    final HashMap<LocalDate, Integer> totalDailyHours = new HashMap<>();
    final List<LocalDateEntryDto> dailyHours =
        myHours.stream()
            .filter(myHour -> !the7thConsecutiveDayTimeLogId.contains(myHour.getTimeLogId()))
            .collect(Collectors.toList());

    // Calculate the time to work more than eight hours and more than twelve hours each day
    for (final LocalDateEntryDto singleEntry : dailyHours) {
      final LocalDate currentDay = singleEntry.getStartTime().toLocalDate();
      totalDailyHours.putIfAbsent(currentDay, 0);
      totalDailyHours.compute(currentDay, (key, val) -> val + singleEntry.getDuration());
      if (totalDailyHours.get(currentDay) > DAILY_OVERTIME_LIMIT_EIGHT_HOUR_CALIFORNIA) {
        final int recentOtMin =
            Math.min(
                totalDailyHours.get(currentDay) - DAILY_OVERTIME_LIMIT_EIGHT_HOUR_CALIFORNIA,
                singleEntry.getDuration());

        final int previousOtMin =
            Math.max(
                0,
                totalDailyHours.get(currentDay)
                    - DAILY_OVERTIME_LIMIT_EIGHT_HOUR_CALIFORNIA
                    - recentOtMin);
        final int currentEightHourOtMin =
            Math.max(
                0,
                Math.min(
                    DAILY_OVERTIME_LIMIT_TWELVE_HOUR_CALIFORNIA
                        - DAILY_OVERTIME_LIMIT_EIGHT_HOUR_CALIFORNIA
                        - previousOtMin,
                    recentOtMin));
        final int currentTwelveHourOtMin = recentOtMin - currentEightHourOtMin;
        if (currentEightHourOtMin > 0) {
          otTracker.addDailyOvertime(
              currentEightHourOtMin, singleEntry, DAILY_OVERTIME_RATE_EIGHT_TO_TWELVE_CALIFORNIA);
        }
        if (currentTwelveHourOtMin > 0) {
          otTracker.addDailyOvertime(
              currentTwelveHourOtMin, singleEntry, DAILY_OVERTIME_RATE_BEYOND_TWELVE_CALIFORNIA);
        }
      }
    }
  }

  private void calculate7thDayOvertime(
      final List<LocalDateEntryDto> weeklyHours,
      final LocalDate firstDayofWeek,
      final Set<String> the7thConsecutiveDayTimeLogId) {
    Integer totalDailyHours = 0;
    LocalDate currentDayOfWeek = firstDayofWeek.plusDays(-1);
    for (final LocalDateEntryDto entry : weeklyHours) {
      if (DAYS.between(currentDayOfWeek, entry.getStartTime().toLocalDate()) > 1) {
        return;
      } else if (entry.getDuration() > 0) {
        currentDayOfWeek = entry.getStartTime().toLocalDate();
        // find the seventh consecutive day
        if (currentDayOfWeek.equals(firstDayofWeek.plusDays(6))) {
          the7thConsecutiveDayTimeLogId.add(entry.getTimeLogId());
          final int minInFirstEightHours =
              Math.max(
                  0,
                  Math.min(
                      DAILY_OVERTIME_LIMIT_EIGHT_HOUR_CALIFORNIA - totalDailyHours,
                      entry.getDuration()));
          final int minAfterFirstEight = entry.getDuration() - minInFirstEightHours;
          if (minInFirstEightHours > 0) {
            otTracker.addDailyOvertime(
                minInFirstEightHours, entry, THE_7TH_CONSECUTIVE_RATE_LIMIT_LESS_EIGHT_HOUR);
          }
          if (minAfterFirstEight > 0) {
            otTracker.addDailyOvertime(
                minAfterFirstEight, entry, THE_7TH_CONSECUTIVE_RATE_LIMIT_BEYOND_EIGHT_HOUR);
          }
          totalDailyHours += entry.getDuration();
        }
      }
    }
  }
}
