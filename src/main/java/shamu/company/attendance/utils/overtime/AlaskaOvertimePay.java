package shamu.company.attendance.utils.overtime;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import shamu.company.attendance.dto.LocalDateEntryDto;

/** @author mshumaker */
public class AlaskaOvertimePay extends FederalOverTimePay {
  private static final Integer DAILY_OVERTIME_LIMIT_ALASKA = 8 * 60;
  private static final Double DAILY_OVERTIME_RATE_ALASKA = 1.5;

  @Override
  public void calculateDailyOvertime(final List<LocalDateEntryDto> myHours) {
    final HashMap<LocalDate, Integer> totalDailyHours = new HashMap<>();
    for (final LocalDateEntryDto singleEntry : myHours) {
      final LocalDate currentDay = singleEntry.getStartTime().toLocalDate();
      totalDailyHours.putIfAbsent(currentDay, 0);
      totalDailyHours.compute(currentDay, (key, val) -> val + singleEntry.getDuration());
      if (totalDailyHours.get(currentDay) > DAILY_OVERTIME_LIMIT_ALASKA) {
        final int otMin =
            Math.min(
                totalDailyHours.get(currentDay) - DAILY_OVERTIME_LIMIT_ALASKA,
                singleEntry.getDuration());
        otTracker.addDailyOvertime(otMin, singleEntry, DAILY_OVERTIME_RATE_ALASKA);
      }
    }
  }
}
