package shamu.company.attendance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.dto.AttendanceSummaryDto;
import shamu.company.attendance.dto.BreakTimeLogDto;
import shamu.company.attendance.dto.MyHoursEntryDto;
import shamu.company.attendance.dto.MyHoursListDto;
import shamu.company.attendance.dto.TimeEntryDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeeTimeEntry;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.StaticEmployeesTaTimeType;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.entity.mapper.EmployeeTimeLogMapper;
import shamu.company.attendance.repository.EmployeeTimeEntryRepository;
import shamu.company.attendance.repository.EmployeeTimeLogRepository;
import shamu.company.attendance.repository.StaticEmployeesTaTimeTypeRepository;
import shamu.company.attendance.service.AttendanceMyHoursService;
import shamu.company.attendance.service.AttendanceSettingsService;
import shamu.company.attendance.service.EmployeeTimeEntryService;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.company.entity.Company;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.CompensationOvertimeStatus;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.service.UserCompensationService;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;

public class AttendanceMyHoursServiceTests {
  @InjectMocks AttendanceMyHoursService attendanceMyHoursService;

  @Mock private EmployeeTimeEntryRepository employeeTimeEntryRepository;

  @Mock private EmployeeTimeLogRepository employeeTimeLogRepository;

  @Mock private StaticEmployeesTaTimeTypeRepository staticEmployeesTaTimeTypeRepository;

  @Mock private UserService userService;

  @Mock private TimeSheetService timeSheetService;

  @Mock private UserCompensationService userCompensationService;

  @Mock private EmployeeTimeLogMapper employeeTimeLogMapper;

  @Mock private EmployeeTimeEntryService employeeTimeEntryService;

  @Mock private AttendanceSettingsService attendanceSettingsService;

  @Mock private TimeOffRequestService timeOffRequestService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class saveTimeEntry {
    BreakTimeLogDto breakTimeLogDto;
    List<BreakTimeLogDto> breakTimeLogDtos;
    TimeEntryDto timeEntryDto;
    User user;
    EmployeeTimeEntry employeeTimeEntry;
    StaticEmployeesTaTimeType breakType;
    StaticEmployeesTaTimeType workType;

    @BeforeEach
    void init() {
      breakTimeLogDto = new BreakTimeLogDto();
      breakTimeLogDtos = new ArrayList<>();
      timeEntryDto = new TimeEntryDto();
      user = new User();
      employeeTimeEntry = new EmployeeTimeEntry();
      breakType = new StaticEmployeesTaTimeType();
      workType = new StaticEmployeesTaTimeType();
      breakType.setName(StaticEmployeesTaTimeType.TimeType.BREAK.name());
      workType.setName(StaticEmployeesTaTimeType.TimeType.WORK.name());
      breakTimeLogDto.setBreakStart(new Timestamp(new Date().getTime()));
      breakTimeLogDto.setBreakMin(30);
      breakTimeLogDto.setTimeType("BREAK");
      timeEntryDto.setStartTime(new Timestamp(new Date().getTime()));
      timeEntryDto.setHoursWorked(1);
      timeEntryDto.setMinutesWorked(1);
      Mockito.when(userService.findById("1")).thenReturn(user);
      Mockito.when(employeeTimeEntryRepository.save(Mockito.any())).thenReturn(employeeTimeEntry);
      Mockito.when(
              staticEmployeesTaTimeTypeRepository.findByName(
                  StaticEmployeesTaTimeType.TimeType.BREAK.name()))
          .thenReturn(breakType);
      Mockito.when(
              staticEmployeesTaTimeTypeRepository.findByName(
                  StaticEmployeesTaTimeType.TimeType.WORK.name()))
          .thenReturn(workType);
    }

    @Test
    void whenNoBreak_thenShouldSuccess() {
      timeEntryDto.setBreakTimeLogs(breakTimeLogDtos);
      assertThatCode(() -> attendanceMyHoursService.saveTimeEntry("1", timeEntryDto))
          .doesNotThrowAnyException();
    }

    @Test
    void whenHasBreakButHasEndTime_thenShouldSuccess() {
      timeEntryDto.setEndTime(new Timestamp(new Date().getTime()));
      breakTimeLogDtos.add(breakTimeLogDto);
      timeEntryDto.setBreakTimeLogs(breakTimeLogDtos);
      assertThatCode(() -> attendanceMyHoursService.saveTimeEntry("1", timeEntryDto))
          .doesNotThrowAnyException();
    }
  }

  @Nested
  class findMyHoursLists {
    List<EmployeeTimeEntry> employeeTimeEntries;
    TimeSheet timeSheet;
    UserCompensation userCompensation;
    CompensationFrequency compensationFrequency;
    EmployeeTimeLog employeeTimeLog;
    List<EmployeeTimeLog> employeeTimeLogs;
    EmployeeTimeEntry employeeTimeEntry;
    StaticEmployeesTaTimeType staticEmployeesTaTimeType;
    MyHoursEntryDto myHoursEntryDto;
    MyHoursListDto myHoursListDto;
    TimePeriod timePeriod;

    @BeforeEach
    void init() {
      timePeriod = new TimePeriod();
      myHoursListDto = new MyHoursListDto();
      myHoursEntryDto = new MyHoursEntryDto();
      myHoursEntryDto.setComments("1");
      myHoursEntryDto.setDate(new Timestamp(new Date().getTime()));
      staticEmployeesTaTimeType = new StaticEmployeesTaTimeType();
      staticEmployeesTaTimeType.setName(StaticEmployeesTaTimeType.TimeType.BREAK.name());
      employeeTimeEntries = new ArrayList<>();
      employeeTimeEntry = new EmployeeTimeEntry();
      employeeTimeEntry.setComment("1");
      userCompensation = new UserCompensation();
      userCompensation.setId("1");
      userCompensation.setWageCents(BigInteger.valueOf(1));
      compensationFrequency = new CompensationFrequency();
      compensationFrequency.setName("HOUR_TYPE");
      userCompensation.setCompensationFrequency(compensationFrequency);
      timeSheet = new TimeSheet();
      timeSheet.setUserCompensation(userCompensation);
      timePeriod.setStartDate(new Timestamp(new Date().getTime()));
      timePeriod.setEndDate(new Timestamp(new Date().getTime()));
      timeSheet.setTimePeriod(timePeriod);
      employeeTimeLog = new EmployeeTimeLog();
      employeeTimeLog.setStart(new Timestamp(new Date().getTime()));
      employeeTimeLog.setTimeType(staticEmployeesTaTimeType);
      employeeTimeLogs = new ArrayList<>();
      employeeTimeLogs.add(employeeTimeLog);
      Mockito.when(timeSheetService.findTimeSheetById(Mockito.anyString())).thenReturn(timeSheet);
      Mockito.when(
              userCompensationService.findCompensationById(timeSheet.getUserCompensation().getId()))
          .thenReturn(userCompensation);
      Mockito.when(employeeTimeLogRepository.findAllByEntryId(employeeTimeEntry.getId()))
          .thenReturn(employeeTimeLogs);
      Mockito.when(
              employeeTimeLogMapper.convertToMyHoursEntryDto(
                  Mockito.any(), Mockito.anyString(), Mockito.anyList()))
          .thenReturn(myHoursEntryDto);
      Mockito.when(employeeTimeLogMapper.convertToMyHoursListDto(Mockito.any(), Mockito.anyList()))
          .thenReturn(myHoursListDto);
    }

    @Test
    void whenTimeEntryIsEmpty_thenShouldSuccess() {
      Mockito.when(employeeTimeEntryService.findEntriesById(Mockito.anyString()))
          .thenReturn(employeeTimeEntries);
      assertThatCode(() -> attendanceMyHoursService.findMyHoursLists("1"))
          .doesNotThrowAnyException();
    }

    @Test
    void whenTimeEntryIsNotEmpty_thenShouldSuccess() {
      employeeTimeEntries.add(employeeTimeEntry);
      Mockito.when(employeeTimeEntryService.findEntriesById(Mockito.anyString()))
          .thenReturn(employeeTimeEntries);
      assertThatCode(() -> attendanceMyHoursService.findMyHoursLists("1"))
          .doesNotThrowAnyException();
    }
  }

  @Test
  void findAttendanceSummary() {
    final TimeSheet timeSheet = new TimeSheet();
    final TimePeriod timePeriod = new TimePeriod();
    timePeriod.setStartDate(Timestamp.valueOf(LocalDateTime.parse("2020-07-01T01:00:00")));
    timePeriod.setEndDate(Timestamp.valueOf(LocalDateTime.parse("2020-07-08T01:00:00")));
    timeSheet.setTimePeriod(timePeriod);
    final UserCompensation userCompensation = new UserCompensation();
    final CompensationFrequency compensationFrequency = new CompensationFrequency();
    compensationFrequency.setName("Per Hour");
    final CompensationOvertimeStatus compensationOvertimeStatus = new CompensationOvertimeStatus();
    compensationOvertimeStatus.setName("California");
    userCompensation.setOvertimeStatus(compensationOvertimeStatus);
    userCompensation.setCompensationFrequency(compensationFrequency);
    userCompensation.setWageCents(BigInteger.valueOf(10));
    timeSheet.setUserCompensation(userCompensation);
    final User user = new User();
    user.setId("1");
    final Company company = new Company();
    company.setId("1");
    user.setCompany(company);
    timeSheet.setEmployee(user);
    final CompanyTaSetting companyTaSetting = new CompanyTaSetting();
    final StaticTimezone staticTimezone = new StaticTimezone();
    staticTimezone.setName("Africa/Abidjan");
    companyTaSetting.setTimeZone(staticTimezone);
    final EmployeeTimeLog employeeTimeLog = new EmployeeTimeLog();
    final StaticEmployeesTaTimeType staticEmployeesTaTimeType = new StaticEmployeesTaTimeType();
    staticEmployeesTaTimeType.setName("WORK");
    employeeTimeLog.setDurationMin(540);
    employeeTimeLog.setStart(Timestamp.valueOf(LocalDateTime.parse("2020-07-02T08:00:00")));
    employeeTimeLog.setTimeType(staticEmployeesTaTimeType);

    final long startOfTimesheetWeek =
        DateUtil.getFirstHourOfWeek(
            timeSheet.getTimePeriod().getStartDate(), companyTaSetting.getTimeZone().getName());
    final long endOfTimesheetWeek =
        DateUtil.getFirstHourOfWeek(
            timeSheet.getTimePeriod().getEndDate(), companyTaSetting.getTimeZone().getName());

    Mockito.when(timeSheetService.findTimeSheetById("1")).thenReturn(timeSheet);
    Mockito.when(attendanceSettingsService.findCompanySettings(user.getCompany().getId()))
        .thenReturn(companyTaSetting);
    Mockito.when(
            timeOffRequestService.findTimeOffHoursBetweenWorkPeriod(
                user.getId(), startOfTimesheetWeek, endOfTimesheetWeek))
        .thenReturn(1);
    Mockito.when(
            employeeTimeLogRepository.findEmployeeTimeLogByTime(
                startOfTimesheetWeek, endOfTimesheetWeek, user.getId()))
        .thenReturn(Collections.singletonList(employeeTimeLog));
    final AttendanceSummaryDto attendanceSummaryDto =
        attendanceMyHoursService.findAttendanceSummary("1");
    assertThat(attendanceSummaryDto.getOverTimeHours()).isEqualTo("1:00");
  }
}
