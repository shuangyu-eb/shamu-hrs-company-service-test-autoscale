package shamu.company.attendance.utils.overtime;

import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.dto.OvertimeDetailDto;
import shamu.company.attendance.dto.OvertimeRuleDto;
import shamu.company.attendance.utils.TimeEntryUtils;
import shamu.company.attendance.utils.overtime.object.HourContainer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OvertimeCalculator {
  HourContainer otTracker;

  private static final String OVERTIME_TYPE_DAILY = "DAILY";
  private static final String OVERTIME_TYPE_WEEKLY = "WEEKLY";

  public List<OvertimeDetailDto> getOvertimePay(
      final List<LocalDateEntryDto> myHours, final Map<String, List<OvertimeRuleDto>> otRules) {
    myHours.sort(TimeEntryUtils.compareByStartDate);
    otTracker = new HourContainer(myHours);
    calculateDailyOvertime(myHours, otRules);
    calculateWeeklyOvertime(myHours, otRules);
    return otTracker.getOvertimeDetails();
  }

  public void calculateDailyOvertime(
      final List<LocalDateEntryDto> myHours, final Map<String, List<OvertimeRuleDto>> otRules) {
    if (otRules.get(OVERTIME_TYPE_DAILY) != null) {
      if (otRules.get(OVERTIME_TYPE_DAILY).size() == 1) {
        oneRateInOneDay(myHours, otRules);
      } else if (otRules.get(OVERTIME_TYPE_DAILY).size() == 2) {
        twoRatesInOneDay(myHours, otRules);
      }
    }
  }

  public void twoRatesInOneDay(
      final List<LocalDateEntryDto> myHours, final Map<String, List<OvertimeRuleDto>> otRules) {

    final Integer dailyStartOne = otRules.get(OVERTIME_TYPE_DAILY).get(0).getStart();
    final Double dailyRateOne = otRules.get(OVERTIME_TYPE_DAILY).get(0).getRate();
    final Integer dailyStartTwo = otRules.get(OVERTIME_TYPE_DAILY).get(1).getStart();
    final Double dailyRateTwo = otRules.get(OVERTIME_TYPE_DAILY).get(1).getRate();
    final HashMap<LocalDate, Integer> totalDailyMinutes = new HashMap<>();

    for (final LocalDateEntryDto singleEntry : myHours) {
      final LocalDate currentDay = singleEntry.getStartTime().toLocalDate();
      totalDailyMinutes.putIfAbsent(currentDay, 0);
      totalDailyMinutes.compute(currentDay, (key, val) -> val + singleEntry.getDuration());
      if (totalDailyMinutes.get(currentDay) > dailyStartOne) {
        final int recentOtMin =
            Math.min(totalDailyMinutes.get(currentDay) - dailyStartOne, singleEntry.getDuration());

        final int previousOtMin =
            Math.max(0, totalDailyMinutes.get(currentDay) - dailyStartOne - recentOtMin);
        final int currentDailyOtOneMin =
            Math.max(0, Math.min(dailyStartTwo - dailyStartOne - previousOtMin, recentOtMin));
        final int currentDailyOtTwoMin = recentOtMin - currentDailyOtOneMin;
        if (currentDailyOtOneMin > 0) {
          otTracker.addDailyOvertime(currentDailyOtOneMin, singleEntry, dailyRateOne);
        }
        if (currentDailyOtTwoMin > 0) {
          otTracker.addDailyOvertime(currentDailyOtTwoMin, singleEntry, dailyRateTwo);
        }
      }
    }
  }

  public void oneRateInOneDay(
      final List<LocalDateEntryDto> myHours, final Map<String, List<OvertimeRuleDto>> otRules) {
    final Integer dailyStart = otRules.get(OVERTIME_TYPE_DAILY).get(0).getStart();
    final Double dailyRate = otRules.get(OVERTIME_TYPE_DAILY).get(0).getRate();
    final HashMap<LocalDate, Integer> totalDailyMinutes = new HashMap<>();
    for (final LocalDateEntryDto singleEntry : myHours) {
      final LocalDate currentDay = singleEntry.getStartTime().toLocalDate();
      totalDailyMinutes.putIfAbsent(currentDay, 0);
      totalDailyMinutes.compute(currentDay, (key, val) -> val + singleEntry.getDuration());
      if (totalDailyMinutes.get(currentDay) > dailyStart) {
        final int otMin =
            Math.min(totalDailyMinutes.get(currentDay) - dailyStart, singleEntry.getDuration());
        otTracker.addDailyOvertime(otMin, singleEntry, dailyRate);
      }
    }
  }

  public void calculateWeeklyOvertime(
      final List<LocalDateEntryDto> myHours, final Map<String, List<OvertimeRuleDto>> otRules) {
    if (otRules.get(OVERTIME_TYPE_WEEKLY) != null) {
      if (otRules.get(OVERTIME_TYPE_WEEKLY).size() == 1) {
        oneRateInOneWeek(myHours, otRules);
      } else if (otRules.get(OVERTIME_TYPE_WEEKLY).size() == 2) {
        twoRatesInOneWeek(myHours, otRules);
      }
    }
  }

  public void oneRateInOneWeek(
      final List<LocalDateEntryDto> myHours, final Map<String, List<OvertimeRuleDto>> otRules) {
    final Integer weeklyStart = otRules.get(OVERTIME_TYPE_WEEKLY).get(0).getStart();
    final Double weeklyRate = otRules.get(OVERTIME_TYPE_WEEKLY).get(0).getRate();
    final HashMap<LocalDate, Integer> totalWeeklyMinutes = new HashMap<>();
    final HashMap<LocalDate, ArrayList<LocalDateEntryDto>> weeklyHourlyEntries = new HashMap<>();
    for (final LocalDateEntryDto singleEntry : myHours) {
      final LocalDate currentWeek = singleEntry.getWeek();
      totalWeeklyMinutes.putIfAbsent(currentWeek, 0);
      weeklyHourlyEntries.putIfAbsent(currentWeek, new ArrayList<>());
      totalWeeklyMinutes.compute(currentWeek, (key, val) -> val + singleEntry.getDuration());
      weeklyHourlyEntries.get(currentWeek).add(0, singleEntry);
      if (totalWeeklyMinutes.get(currentWeek)
          > weeklyStart + otTracker.getTotalDailyOt(currentWeek)) {
        final int otMin =
            Math.min(
                totalWeeklyMinutes.get(currentWeek)
                    - (weeklyStart + otTracker.getTotalDailyOt(currentWeek)),
                singleEntry.getDuration());
        otTracker.addWeeklyOvertime(otMin, weeklyHourlyEntries.get(currentWeek), weeklyRate);
      }
    }
  }

  public void twoRatesInOneWeek(
      final List<LocalDateEntryDto> myHours, final Map<String, List<OvertimeRuleDto>> otRules) {
    final Integer weeklyStartOne = otRules.get(OVERTIME_TYPE_WEEKLY).get(0).getStart();
    final Double weeklyRateOne = otRules.get(OVERTIME_TYPE_WEEKLY).get(0).getRate();
    final Integer weeklyStartTwo = otRules.get(OVERTIME_TYPE_WEEKLY).get(1).getStart();
    final Double weeklyRateTwo = otRules.get(OVERTIME_TYPE_WEEKLY).get(1).getRate();
    final HashMap<LocalDate, Integer> totalWeeklyMinutes = new HashMap<>();
    final HashMap<LocalDate, ArrayList<LocalDateEntryDto>> weeklyHourlyEntries = new HashMap<>();
    for (final LocalDateEntryDto singleEntry : myHours) {
      final LocalDate currentWeek = singleEntry.getWeek();
      totalWeeklyMinutes.putIfAbsent(currentWeek, 0);
      weeklyHourlyEntries.putIfAbsent(currentWeek, new ArrayList<>());
      totalWeeklyMinutes.compute(currentWeek, (key, val) -> val + singleEntry.getDuration());
      weeklyHourlyEntries.get(currentWeek).add(0, singleEntry);
      final Integer weeklyStartAndDailyOt = weeklyStartOne + otTracker.getTotalDailyOt(currentWeek);
      if (totalWeeklyMinutes.get(currentWeek) > weeklyStartAndDailyOt) {
        final int recentOtMin =
            Math.min(
                totalWeeklyMinutes.get(currentWeek) - weeklyStartAndDailyOt,
                singleEntry.getDuration());
        final int previousOtMin =
            Math.max(0, totalWeeklyMinutes.get(currentWeek) - weeklyStartAndDailyOt - recentOtMin);

        final int currentWeeklyOtOneMin =
            Math.max(
                0, Math.min(weeklyStartTwo - weeklyStartAndDailyOt - previousOtMin, recentOtMin));
        final int currentWeeklyOtTwoMin = recentOtMin - currentWeeklyOtOneMin;
        if (currentWeeklyOtOneMin > 0) {
          otTracker.addWeeklyOvertime(
              currentWeeklyOtOneMin, weeklyHourlyEntries.get(currentWeek), weeklyRateOne);
        }
        if (currentWeeklyOtTwoMin > 0) {
          otTracker.addWeeklyOvertime(
              currentWeeklyOtTwoMin, weeklyHourlyEntries.get(currentWeek), weeklyRateTwo);
        }
      }
    }
  }

  public HourContainer getOtTracker() {
    return otTracker;
  }
}
