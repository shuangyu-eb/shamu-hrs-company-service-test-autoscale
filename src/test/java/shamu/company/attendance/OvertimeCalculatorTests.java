package shamu.company.attendance;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.StaticEmployeesTaTimeType;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.attendance.utils.TimeEntryUtils;
import shamu.company.attendance.utils.overtime.AlaskaOvertimePay;
import shamu.company.attendance.utils.overtime.CaliforniaOvertimePay;
import shamu.company.attendance.utils.overtime.ColoradoOvertime;
import shamu.company.attendance.utils.overtime.FederalOverTimePay;
import shamu.company.attendance.utils.overtime.KentuckyOvertime;

/** @author mshumaker */
public class OvertimeCalculatorTests {
  ArrayList<EmployeeTimeLog> weeklyHours;
  RandomValueStringGenerator randomValueStringGenerator;
  String timezoneLoc;
  Long startTime;
  Long endTime;
  List<LocalDateEntryDto> localDates;
  StaticTimezone timezone;

  @BeforeEach
  void init() {
    weeklyHours = new ArrayList<>();
    randomValueStringGenerator = new RandomValueStringGenerator();
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
      final EmployeeTimeLog nineHourDay = createDay(day, timezoneLoc, "09:00:00", 9);
      weeklyHours.add(nineHourDay);
    }
    timezone = new StaticTimezone();
    timezone.setName(timezoneLoc);
  }

  private EmployeeTimeLog createDay(final String day, final String timezone, final String startTime, final Integer length) {
    final EmployeeTimeLog timeLog = new EmployeeTimeLog();
    final ZoneId currentZone = ZoneId.of(timezone);
    final long workStart = LocalDateTime.parse(day + "T" + startTime).atZone(currentZone).toEpochSecond();
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
  void federalOvertimeCheck() {
    // EmployeeTimeLog twoHourDay = createDay("2020-06-25",timezoneLoc,"20:00:00",6);
    // weeklyHours.add(twoHourDay);
    localDates = TimeEntryUtils.transformTimeLogsToLocalDate(weeklyHours, timezone);
    final FederalOverTimePay federalOverTimePay = new FederalOverTimePay();
    federalOverTimePay.getOvertimePay(localDates);
    final Integer hourlyOt =
        federalOverTimePay.getOtTracker().getTotalWeeklyOt().get(LocalDate.parse("2020-06-20"));
    assertThat(hourlyOt).isEqualTo(23 * 60);
  }

  @Test
  void alaskaOvertimeCheck() {
    localDates = TimeEntryUtils.transformTimeLogsToLocalDate(weeklyHours, timezone);
    final AlaskaOvertimePay alaskaOvertimePay = new AlaskaOvertimePay();
    alaskaOvertimePay.getOvertimePay(localDates);
    final Integer hourlyOt =
        alaskaOvertimePay.getOtTracker().getTotalWeeklyOt().get(LocalDate.parse("2020-06-20"));
    assertThat(hourlyOt).isEqualTo(16 * 60);
  }

  @Test
  void basicColoradoOvertimeCheck() {
    // EmployeeTimeLog twoHourDay = createDay("2020-06-25",timezoneLoc,"20:00:00",6);
    // weeklyHours.add(twoHourDay);
    localDates = TimeEntryUtils.transformTimeLogsToLocalDate(weeklyHours, timezone);
    final ColoradoOvertime coloradoOvertime = new ColoradoOvertime();
    coloradoOvertime.getOvertimePay(localDates);
    final Integer hourlyOt =
        coloradoOvertime.getOtTracker().getTotalWeeklyOt().get(LocalDate.parse("2020-06-20"));
    assertThat(hourlyOt).isEqualTo(23 * 60);
  }

  @Test
  void twelveHourDayOvertimeCheck() {
    final EmployeeTimeLog extraTime = createDay("2020-06-26", timezoneLoc, "02:00:00", 4);
    weeklyHours.add(extraTime);
    localDates = TimeEntryUtils.transformTimeLogsToLocalDate(weeklyHours, timezone);
    final ColoradoOvertime coloradoOvertime = new ColoradoOvertime();
    coloradoOvertime.getOvertimePay(localDates);
    final Integer hourlyOt =
        coloradoOvertime.getOtTracker().getTotalDailyOt().get(LocalDate.parse("2020-06-20"));
    assertThat(hourlyOt).isEqualTo(1 * 60);
  }

  @Test
  void twelveHourShiftOvertimeCheck() {
    weeklyHours = new ArrayList<>();
    final ArrayList<String> weeklyData = new ArrayList<>();
    weeklyData.add("2020-06-20");
    weeklyData.add("2020-06-21");
    weeklyData.add("2020-06-22");
    weeklyData.add("2020-06-23");
    weeklyData.add("2020-06-24");
    weeklyData.add("2020-06-25");
    weeklyData.add("2020-06-28");
    weeklyData.add("2020-06-29");
    weeklyData.add("2020-06-30");
    weeklyData.add("2020-07-01");
    weeklyData.add("2020-07-02");
    weeklyData.add("2020-07-03");
    timezoneLoc = "America/Chicago";
    for (final String day : weeklyData) {
      final EmployeeTimeLog nineHourDay = createDay(day, timezoneLoc, "09:00:00", 8);
      weeklyHours.add(nineHourDay);
    }
    final EmployeeTimeLog extraTime = createDay("2020-06-26", timezoneLoc, "18:00:00", 14);
    weeklyHours.add(extraTime);
    localDates = TimeEntryUtils.transformTimeLogsToLocalDate(weeklyHours, timezone);
    final ColoradoOvertime coloradoOvertime = new ColoradoOvertime();
    coloradoOvertime.getOvertimePay(localDates);
    final Integer hourlyOt =
        coloradoOvertime.getOtTracker().getTotalDailyOt().get(LocalDate.parse("2020-06-27"));
    assertThat(hourlyOt).isEqualTo(2 * 60);
  }

  @Test
  void differentOTtypesDifferentWeeks() {
    weeklyHours = new ArrayList<>();
    final ArrayList<String> weeklyData = new ArrayList<>();
    weeklyData.add("2020-06-20");
    weeklyData.add("2020-06-21");
    weeklyData.add("2020-06-22");
    weeklyData.add("2020-06-23");
    weeklyData.add("2020-06-24");
    weeklyData.add("2020-06-25");
    weeklyData.add("2020-06-28");
    weeklyData.add("2020-06-29");
    weeklyData.add("2020-06-30");
    weeklyData.add("2020-07-01");
    weeklyData.add("2020-07-02");
    weeklyData.add("2020-07-03");
    timezoneLoc = "America/Chicago";
    for (final String day : weeklyData) {
      final EmployeeTimeLog nineHourDay = createDay(day, timezoneLoc, "09:00:00", 8);
      weeklyHours.add(nineHourDay);
    }
    final EmployeeTimeLog extraTime = createDay("2020-06-26", timezoneLoc, "18:00:00", 14);
    weeklyHours.add(extraTime);
    final EmployeeTimeLog extraTime2 = createDay("2020-06-26", timezoneLoc, "01:00:00", 9);
    weeklyHours.add(extraTime2);
    localDates = TimeEntryUtils.transformTimeLogsToLocalDate(weeklyHours, timezone);
    final ColoradoOvertime coloradoOvertime = new ColoradoOvertime();
    coloradoOvertime.getOvertimePay(localDates);
    final Integer hourlyOt1 =
        coloradoOvertime.getOtTracker().getTotalDailyOt().get(LocalDate.parse("2020-06-20"));
    final Integer hourlyOt2 =
        coloradoOvertime.getOtTracker().getTotalDailyOt().get(LocalDate.parse("2020-06-27"));
    assertThat(hourlyOt2).isEqualTo(2 * 60);
    assertThat(hourlyOt1).isEqualTo(3 * 60);
  }

  @Test
  void doubleCountCheck() {
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
      final EmployeeTimeLog nineHourDay = createDay(day, timezoneLoc, "09:00:00", 8);
      weeklyHours.add(nineHourDay);
    }
    final EmployeeTimeLog extraTime = createDay("2020-06-26", timezoneLoc, "19:00:00", 3);
    weeklyHours.add(extraTime);
    localDates = TimeEntryUtils.transformTimeLogsToLocalDate(weeklyHours, timezone);
    final AlaskaOvertimePay alaskaOvertimePay = new AlaskaOvertimePay();
    alaskaOvertimePay.getOvertimePay(localDates);
    final Integer hourlyOt =
        alaskaOvertimePay.getOtTracker().getTotalWeeklyOt().get(LocalDate.parse("2020-06-20"));
  }

  @Test
  void basicKentuckyOvertimeTest() {
    localDates = TimeEntryUtils.transformTimeLogsToLocalDate(weeklyHours, timezone);
    final KentuckyOvertime kentuckyOvertime = new KentuckyOvertime();
    kentuckyOvertime.getOvertimePay(localDates);
    final Integer weeklyOt =
        kentuckyOvertime.getOtTracker().getTotalWeeklyOt().get(LocalDate.parse("2020-06-20"));
    final Integer dailyOt =
        kentuckyOvertime.getOtTracker().getTotalDailyOt().get(LocalDate.parse("2020-06-20"));
    assertThat(weeklyOt).isEqualTo(14 * 60);
    assertThat(dailyOt).isEqualTo(9 * 60);
  }

  @Test
  void basicCaliforniaOvertimeTest() {
    localDates = TimeEntryUtils.transformTimeLogsToLocalDate(weeklyHours, timezone);
    final CaliforniaOvertimePay californiaOvertimePay = new CaliforniaOvertimePay();
    californiaOvertimePay.getOvertimePay(localDates);
    final Integer weeklyOt =
        californiaOvertimePay.getOtTracker().getTotalWeeklyOt().get(LocalDate.parse("2020-06-20"));
    final Integer dailyOt =
        californiaOvertimePay.getOtTracker().getTotalDailyOt().get(LocalDate.parse("2020-06-20"));
    assertThat(weeklyOt).isEqualTo(8 * 60);
    assertThat(dailyOt).isEqualTo(15 * 60);
  }
}
