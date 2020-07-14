package shamu.company.attendance.utils.overtime.object;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.Data;
import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.dto.OverTimeMinutesDto;
import shamu.company.attendance.dto.OvertimeDetailDto;

/** @author mshumaker */
@Data
public class HourContainer {
  ArrayList<OvertimeDetailDto> overtimeDetails;
  HashMap<String, OvertimeDetailDto> timeLogToOvertimeDetails;
  HashMap<String, Integer> totalTimeLogMinutes;
  HashMap<LocalDate, Integer> totalDailyOt;
  HashMap<LocalDate, Integer> totalWeeklyOt;

  public HourContainer(final List<LocalDateEntryDto> timeLogs) {
    overtimeDetails = new ArrayList<>();
    timeLogToOvertimeDetails = new HashMap<>();
    totalDailyOt = new HashMap<>();
    totalWeeklyOt = new HashMap<>();
    totalTimeLogMinutes = new HashMap<>();
    for (final LocalDateEntryDto localDateEntryDto : timeLogs) {
      final String id = localDateEntryDto.getTimeLogId();
      totalTimeLogMinutes.putIfAbsent(id, 0);
      totalTimeLogMinutes.compute(id, (key, val) -> val + localDateEntryDto.getDuration());
    }
  }
  // ASSUMING NO OVERLAP IE WE ONLY ADD DAILY OVERTIME 1 TIME... THIS SHOULD BE UPDATED IN FUTURE
  // TO WHERE WE WON'T EVER DOUBLE COUNT OVERTIME... Example. CURRENTLY FOR CALIFORNIA, IF A USER
  // QUALIFIES FOR DAILY OVERTIME BY WORKING 7 DAYS A WEEK AND BY WORKING OVER 8 HOURS IN THAT DAY
  // WE SHOULD HAVE A CHHECK HERE TO MAKE SURE THE OVERTIME HASN'T BEEN DOUBLE COUNTED
  public void addDailyOvertime(
      final int totMin, final LocalDateEntryDto localDateEntryDto, final Double rate) {
    final String entryId = localDateEntryDto.getTimeLogId();
    final OvertimeDetailDto otDetails;
    // One time log may be divided into two time logs with different rates
    if (timeLogToOvertimeDetails.containsKey(entryId)) {
      otDetails = timeLogToOvertimeDetails.get(entryId);
    } else {
      otDetails = new OvertimeDetailDto();
      otDetails.setTimeLogId(entryId);
      otDetails.setTotalMinutes(0);
      otDetails.setOverTimeMinutesDtos(new ArrayList<>());
      timeLogToOvertimeDetails.put(entryId, otDetails);
      overtimeDetails.add(otDetails);
    }
    final OverTimeMinutesDto overTimeMinutesDto = new OverTimeMinutesDto();
    overTimeMinutesDto.setRate(rate);
    overTimeMinutesDto.setMinutes(totMin);
    overTimeMinutesDto.setTimeLogId(localDateEntryDto.getTimeLogId());
    overTimeMinutesDto.setStartTime(localDateEntryDto.getStartTime());
    overTimeMinutesDto.setType("daily");
    otDetails.setTotalMinutes(otDetails.getTotalMinutes() + totMin);
    otDetails.addMinuteDto(overTimeMinutesDto);
    addDailyMin(totMin, localDateEntryDto);
  }

  public void addWeeklyOvertime(
      int totMin, final List<LocalDateEntryDto> orderedWorkedHours, final Double rate) {
    int currentIndex = 0;
    while (totMin > 0) {
      final LocalDateEntryDto localDateEntryDto = orderedWorkedHours.get(currentIndex);
      totMin = totMin - addSingleWeeklyOvertimeEntry(totMin, localDateEntryDto, rate);
      currentIndex += 1;
    }
  }

  private int addSingleWeeklyOvertimeEntry(
      final int maxMin, final LocalDateEntryDto entry, final Double rate) {
    final String entryId = entry.getTimeLogId();
    final OvertimeDetailDto otDetails;
    if (timeLogToOvertimeDetails.containsKey(entryId)) {
      otDetails = timeLogToOvertimeDetails.get(entryId);
    } else {
      otDetails = new OvertimeDetailDto();
      otDetails.setTimeLogId(entryId);
      otDetails.setTotalMinutes(0);
      otDetails.setOverTimeMinutesDtos(new ArrayList<>());
      timeLogToOvertimeDetails.put(entryId, otDetails);
      overtimeDetails.add(otDetails);
    }
    final int entryMinutesLeft = totalTimeLogMinutes.get(entryId) - otDetails.getTotalMinutes();
    final int totalMinutesToAdd = Math.min(entryMinutesLeft, maxMin);
    final OverTimeMinutesDto overTimeMinutesDto = new OverTimeMinutesDto();
    overTimeMinutesDto.setRate(rate);
    overTimeMinutesDto.setMinutes(totalMinutesToAdd);
    overTimeMinutesDto.setTimeLogId(entry.getTimeLogId());
    overTimeMinutesDto.setStartTime(entry.getStartTime());
    overTimeMinutesDto.setType("weekly");
    otDetails.setTotalMinutes(otDetails.getTotalMinutes() + totalMinutesToAdd);
    otDetails.addMinuteDto(overTimeMinutesDto);
    addWeeklyMin(totalMinutesToAdd, entry);
    return totalMinutesToAdd;
  }

  private void addDailyMin(final int totalMinutesToAdd, final LocalDateEntryDto entry) {
    final LocalDate currentWeek = entry.getWeek();
    if (totalDailyOt.containsKey(currentWeek)) {
      final int dailyOt = totalDailyOt.get(currentWeek);
      totalDailyOt.put(currentWeek, dailyOt + totalMinutesToAdd);
    } else {
      totalDailyOt.put(currentWeek, totalMinutesToAdd);
    }
  }

  private void addWeeklyMin(final int totalMinutesToAdd, final LocalDateEntryDto entry) {
    final LocalDate currentWeek = entry.getWeek();
    if (totalWeeklyOt.containsKey(currentWeek)) {
      final int weeklyOt = totalWeeklyOt.get(currentWeek);
      totalWeeklyOt.put(currentWeek, weeklyOt + totalMinutesToAdd);
    } else {
      totalWeeklyOt.put(currentWeek, totalMinutesToAdd);
    }
  }

  public Integer getTotalDailyOt(final LocalDate startOfWeek) {
    if (totalDailyOt.containsKey(startOfWeek)) {
      return totalDailyOt.get(startOfWeek);
    }
    return 0;
  }
}
