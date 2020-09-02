package shamu.company.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.dto.OvertimeRuleDto;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.StaticEmployeesTaTimeType;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.attendance.utils.TimeEntryUtils;
import shamu.company.attendance.utils.overtime.OvertimeCalculator;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class NewOvertimeCalculatorTests {
  List<LocalDateEntryDto> localDateEntryDtos;
  LocalDateEntryDto localDateEntryDto;
  List<OvertimeRuleDto> overtimeRuleDtos;
  List<OvertimeRuleDto> overtimeRuleDtos2;
  OvertimeRuleDto overtimeRuleDto1;
  OvertimeRuleDto overtimeRuleDto2;
  OvertimeRuleDto overtimeRuleDto3;
  OvertimeRuleDto overtimeRuleDto4;
  ArrayList<EmployeeTimeLog> weeklyHours;
  StaticTimezone timezone;
  String timezoneLoc;
  RandomValueStringGenerator randomValueStringGenerator;
  List<LocalDateEntryDto> localDates;
  Map<String, List<OvertimeRuleDto>> otRules;

  @BeforeEach
  void init() {
    localDateEntryDtos = new ArrayList<>();
    localDateEntryDto = new LocalDateEntryDto();
    randomValueStringGenerator = new RandomValueStringGenerator();
    overtimeRuleDtos = new ArrayList<>();
    overtimeRuleDtos2 = new ArrayList<>();
    overtimeRuleDto1 = new OvertimeRuleDto();
    overtimeRuleDto1.setStart(8 * 60);
    overtimeRuleDto1.setRate(1.5);
    overtimeRuleDto2 = new OvertimeRuleDto();
    overtimeRuleDto2.setStart(12 * 60);
    overtimeRuleDto2.setRate(2.0);
    overtimeRuleDto3 = new OvertimeRuleDto();
    overtimeRuleDto4 = new OvertimeRuleDto();
    overtimeRuleDto3.setStart(40 * 60);
    overtimeRuleDto3.setRate(1.5);
    overtimeRuleDto4.setStart(60 * 60);
    overtimeRuleDto4.setRate(2.0);
    weeklyHours = new ArrayList<>();
    final ArrayList<String> weeklyData = new ArrayList<>();
    weeklyData.add("2020-06-20");
    weeklyData.add("2020-06-21");
    weeklyData.add("2020-06-22");
    weeklyData.add("2020-06-23");
    weeklyData.add("2020-06-24");
    weeklyData.add("2020-06-25");
    weeklyData.add("2020-06-26");
    weeklyData.add("2020-06-27");
    weeklyData.add("2020-06-28");
    weeklyData.add("2020-06-29");
    weeklyData.add("2020-06-30");
    weeklyData.add("2020-07-01");
    weeklyData.add("2020-07-02");
    weeklyData.add("2020-07-03");
    timezoneLoc = "America/Chicago";
    for (final String day : weeklyData) {
      final EmployeeTimeLog nineHourDay = createDay(day, timezoneLoc, "09:00:00", 13);
      weeklyHours.add(nineHourDay);
    }
    timezone = new StaticTimezone();
    timezone.setName(timezoneLoc);
    otRules = new HashMap<>();
  }

  private EmployeeTimeLog createDay(
      final String day, final String timezone, final String startTime, final Integer length) {
    final EmployeeTimeLog timeLog = new EmployeeTimeLog();
    final ZoneId currentZone = ZoneId.of(timezone);
    final long workStart =
        LocalDateTime.parse(day + "T" + startTime).atZone(currentZone).toEpochSecond();
    final Timestamp startime = new Timestamp(workStart * 1000);
    final StaticEmployeesTaTimeType staticEmployeesTaTimeType = new StaticEmployeesTaTimeType();
    staticEmployeesTaTimeType.setName("work");
    timeLog.setDurationMin(length * 60);
    timeLog.setStart(startime);
    timeLog.setTimeType(staticEmployeesTaTimeType);
    timeLog.setId(randomValueStringGenerator.generate());
    return timeLog;
  }

  @Test
  void toneRateOvertimeCheck() {
    overtimeRuleDtos.add(overtimeRuleDto1);
    overtimeRuleDtos2.add(overtimeRuleDto3);
    otRules.put("DAILY", overtimeRuleDtos);
    otRules.put("WEEKLY", overtimeRuleDtos2);
    localDates = TimeEntryUtils.transformTimeLogsToLocalDate(weeklyHours, timezone);
    final OvertimeCalculator overtimeCalculator = new OvertimeCalculator();
    overtimeCalculator.getOvertimePay(localDates, otRules);
    final Integer weeklyOt =
        overtimeCalculator.getOtTracker().getTotalWeeklyOt().get(LocalDate.parse("2020-06-20"));
    final Integer dailyOt =
        overtimeCalculator.getOtTracker().getTotalDailyOt().get(LocalDate.parse("2020-06-20"));
    assertThat(weeklyOt).isEqualTo(16 * 60);
    assertThat(dailyOt).isEqualTo(35 * 60);
  }

  @Test
  void twoRatesOvertimeCheck() {
    overtimeRuleDtos.add(overtimeRuleDto1);
    overtimeRuleDtos.add(overtimeRuleDto2);
    overtimeRuleDtos2.add(overtimeRuleDto3);
    overtimeRuleDtos2.add(overtimeRuleDto4);
    otRules.put("DAILY", overtimeRuleDtos);
    otRules.put("WEEKLY", overtimeRuleDtos2);
    localDates = TimeEntryUtils.transformTimeLogsToLocalDate(weeklyHours, timezone);
    final OvertimeCalculator overtimeCalculator = new OvertimeCalculator();
    overtimeCalculator.getOvertimePay(localDates, otRules);
    final Integer weeklyOt =
        overtimeCalculator.getOtTracker().getTotalWeeklyOt().get(LocalDate.parse("2020-06-20"));
    final Integer dailyOt =
        overtimeCalculator.getOtTracker().getTotalDailyOt().get(LocalDate.parse("2020-06-20"));
    assertThat(weeklyOt).isEqualTo(16 * 60);
    assertThat(dailyOt).isEqualTo(35 * 60);
  }
}
