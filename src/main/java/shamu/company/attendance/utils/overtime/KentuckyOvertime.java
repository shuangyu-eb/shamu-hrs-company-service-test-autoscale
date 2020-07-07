package shamu.company.attendance.utils.overtime;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import shamu.company.attendance.dto.LocalDateEntryDto;

/** @author mshumaker */
public class KentuckyOvertime extends FederalOverTimePay {
  private static final Double DAILY_OVERTIME_RATE_KENTUCKY = 1.5;

  @Override
  public void calculateDailyOvertime(final List<LocalDateEntryDto> myHours) {
    final HashSet<LocalDate> weeks = new HashSet<>();
    for (final LocalDateEntryDto entry : myHours) {
      weeks.add(entry.getWeek());
    }
    for (final LocalDate week : weeks) {
      final List<LocalDateEntryDto> weeklyHours =
          myHours.stream()
              .filter(myHour -> myHour.getWeek().equals(week))
              .collect(Collectors.toList());
      calculate7thDayOvertime(weeklyHours, week);
    }
  }

  private void calculate7thDayOvertime(
          final List<LocalDateEntryDto> weeklyHours, final LocalDate firstDayofWeek) {
    LocalDate currentDayOfWeek = firstDayofWeek.plusDays(-1);
    for (final LocalDateEntryDto entry : weeklyHours) {
      if (DAYS.between(currentDayOfWeek, entry.getStartTime().toLocalDate()) > 1) {
        return;
      } else if (entry.getDuration() > 0) {
        currentDayOfWeek = entry.getStartTime().toLocalDate();
        if (currentDayOfWeek.equals(firstDayofWeek.plusDays(6))) {
          otTracker.addDailyOvertime(entry.getDuration(), entry, DAILY_OVERTIME_RATE_KENTUCKY);
        }
      }
    }
  }
}
