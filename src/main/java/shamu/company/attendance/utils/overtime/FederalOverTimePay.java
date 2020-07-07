package shamu.company.attendance.utils.overtime;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.dto.OvertimeDetailDto;
import shamu.company.attendance.utils.TimeEntryUtils;
import shamu.company.attendance.utils.overtime.object.HourContainer;

/** @author mshumaker */
public class FederalOverTimePay implements OverTimePay {
  HourContainer otTracker;

  static final int WEEKLY_MINUTES = 40 * 60;
  static final double FEDERAL_RATE = 1.5;

  @Override
  public ArrayList<OvertimeDetailDto> getOvertimePay(final List<LocalDateEntryDto> myHours) {
    myHours.sort(TimeEntryUtils.compareByStartDate);
    otTracker = new HourContainer(myHours);
    calculateDailyOvertime(myHours);
    calculateWeeklyOvertime(myHours);
    return otTracker.getOvertimeDetails();
  }

  public void calculateWeeklyOvertime(final List<LocalDateEntryDto> myHours) {
    final HashMap<LocalDate, Integer> totalWeeklyHours = new HashMap<>();
    final HashMap<LocalDate, ArrayList<LocalDateEntryDto>> weeklyHourlyEntries = new HashMap<>();
    for (final LocalDateEntryDto singleEntry : myHours) {
      final LocalDate currentWeek = singleEntry.getWeek();
      totalWeeklyHours.putIfAbsent(currentWeek, 0);
      weeklyHourlyEntries.putIfAbsent(currentWeek, new ArrayList<>());
      totalWeeklyHours.compute(currentWeek, (key, val) -> val + singleEntry.getDuration());
      weeklyHourlyEntries.get(currentWeek).add(0, singleEntry);
      if (totalWeeklyHours.get(currentWeek)
          > WEEKLY_MINUTES + otTracker.getTotalDailyOt(currentWeek)) {
        final int otMin =
            Math.min(
                totalWeeklyHours.get(currentWeek)
                    - (WEEKLY_MINUTES + otTracker.getTotalDailyOt(currentWeek)),
                singleEntry.getDuration());
        otTracker.addWeeklyOvertime(otMin, weeklyHourlyEntries.get(currentWeek), FEDERAL_RATE);
      }
    }
  }

  public HourContainer getOtTracker() {
    return otTracker;
  }

  public void calculateDailyOvertime(final List<LocalDateEntryDto> myHours) {
    return;
  }
}
