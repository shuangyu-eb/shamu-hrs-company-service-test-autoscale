package shamu.company.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.dto.AllTimeEntryDto;
import shamu.company.attendance.dto.AttendanceSummaryDto;
import shamu.company.attendance.dto.BreakTimeLogDto;
import shamu.company.attendance.dto.EmployeesTaSettingDto;
import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.dto.OverTimeMinutesDto;
import shamu.company.attendance.dto.OvertimeDetailDto;
import shamu.company.attendance.dto.TimeEntryDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeeTimeEntry;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.EmployeesTaSetting;
import shamu.company.attendance.entity.StaticEmployeesTaTimeType;
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.Timesheet;
import shamu.company.attendance.entity.mapper.EmployeeTimeLogMapper;
import shamu.company.attendance.repository.EmployeeTimeEntryRepository;
import shamu.company.attendance.repository.EmployeeTimeLogRepository;
import shamu.company.attendance.repository.StaticEmployeesTaTimeTypeRepository;
import shamu.company.attendance.repository.StaticTimesheetStatusRepository;
import shamu.company.attendance.service.AttendanceMyHoursService;
import shamu.company.attendance.service.AttendanceSettingsService;
import shamu.company.attendance.service.EmployeeTimeEntryService;
import shamu.company.attendance.service.EmployeesTaSettingService;
import shamu.company.attendance.service.GenericHoursService;
import shamu.company.attendance.service.OvertimeService;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.company.entity.Company;
import shamu.company.email.service.EmailService;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.service.UserCompensationService;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class AttendanceMyHoursServiceTests {

  private static final int CONVERT_SECOND_TO_MS = 1000;
  private final EmployeeTimeLogMapper employeeTimeLogMapper =
      Mappers.getMapper(EmployeeTimeLogMapper.class);

  @Mock private EmployeeTimeEntryRepository employeeTimeEntryRepository;

  @Mock private EmployeeTimeLogRepository employeeTimeLogRepository;

  @Mock private StaticEmployeesTaTimeTypeRepository staticEmployeesTaTimeTypeRepository;

  @Mock private UserService userService;

  @Mock private TimeSheetService timeSheetService;

  @Mock private UserCompensationService userCompensationService;

  @Mock private EmployeeTimeEntryService employeeTimeEntryService;

  @Mock private AttendanceSettingsService attendanceSettingsService;

  @Mock private TimeOffRequestService timeOffRequestService;

  @Mock private EmployeesTaSettingService employeesTaSettingService;

  @Mock private OvertimeService overtimeService;

  @Mock private GenericHoursService genericHoursService;

  @Mock private TimePeriodService timePeriodService;

  @Mock private StaticTimesheetStatusRepository staticTimesheetStatusRepository;

  @Mock private EmailService emailService;

  private AttendanceMyHoursService attendanceMyHoursService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    attendanceMyHoursService =
        new AttendanceMyHoursService(
            employeeTimeEntryService,
            employeeTimeLogRepository,
            timeSheetService,
            userCompensationService,
            staticEmployeesTaTimeTypeRepository,
            userService,
            employeeTimeLogMapper,
            attendanceSettingsService,
            timeOffRequestService,
            overtimeService,
            genericHoursService,
            staticTimesheetStatusRepository,
            employeesTaSettingService,
            timePeriodService,
            emailService);
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
    EmployeesTaSettingDto employeesTaSetting = new EmployeesTaSettingDto();
    StaticTimezone staticTimezone = new StaticTimezone();

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
      breakTimeLogDto.setTimeType("BREAK");
      timeEntryDto.setStartTime(new Timestamp(new Date().getTime()));
      timeEntryDto.setEndTime(new Timestamp(new Date().getTime() + 30 * 60 * 1000));
      timeEntryDto.setHoursWorked(1);
      timeEntryDto.setMinutesWorked(1);
      staticTimezone.setName("Hongkong");
      employeesTaSetting.setTimeZone(staticTimezone);
      user.setId("1");
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
      timeEntryDto.setHoursWorked(1);
      timeEntryDto.setMinutesWorked(1);
      timeEntryDto.setBreakTimeLogs(breakTimeLogDtos);
      Mockito.when(userService.findById("1")).thenReturn(user);
      assertThatCode(() -> attendanceMyHoursService.saveTimeEntry("1", timeEntryDto, "1"))
          .doesNotThrowAnyException();
    }

    @Test
    void whenHasBreakButHasEndTime_thenShouldSuccess() {
      timeEntryDto.setHoursWorked(1);
      timeEntryDto.setMinutesWorked(1);
      breakTimeLogDto.setBreakMin(30);
      breakTimeLogDtos.add(breakTimeLogDto);
      timeEntryDto.setBreakTimeLogs(breakTimeLogDtos);
      Mockito.when(attendanceSettingsService.findEmployeesSettings(Mockito.any()))
          .thenReturn(employeesTaSetting);
      Mockito.when(userService.findById("1")).thenReturn(user);
      assertThatCode(() -> attendanceMyHoursService.saveTimeEntry("1", timeEntryDto, "1"))
          .doesNotThrowAnyException();
    }

    @Test
    void whenHasBreakButBreakEndTimeEqualsEndTime_thenShouldSuccess() {
      timeEntryDto.setHoursWorked(0);
      timeEntryDto.setMinutesWorked(0);
      breakTimeLogDto.setBreakMin(20);
      breakTimeLogDtos.add(breakTimeLogDto);
      timeEntryDto.setBreakTimeLogs(breakTimeLogDtos);

      Mockito.when(userService.findById("1")).thenReturn(user);
      Mockito.when(attendanceSettingsService.findEmployeesSettings(Mockito.any()))
          .thenReturn(employeesTaSetting);
      assertThatCode(() -> attendanceMyHoursService.saveTimeEntry("1", timeEntryDto, "1"))
          .doesNotThrowAnyException();
    }
  }

  @Test
  void findAttendanceSummary() {
    final Timesheet timesheet = new Timesheet();
    final TimePeriod timePeriod = new TimePeriod();
    timePeriod.setStartDate(Timestamp.valueOf(LocalDateTime.parse("2020-07-06T00:00:00")));
    timePeriod.setEndDate(Timestamp.valueOf(LocalDateTime.parse("2020-07-11T00:00:00")));
    timesheet.setTimePeriod(timePeriod);
    final UserCompensation userCompensation = new UserCompensation();
    final CompensationFrequency compensationFrequency = new CompensationFrequency();
    compensationFrequency.setName("Per Hour");
    userCompensation.setCompensationFrequency(compensationFrequency);
    userCompensation.setWageCents(BigInteger.valueOf(10));
    timesheet.setUserCompensation(userCompensation);
    final User user = new User();
    user.setId("1");
    final Company company = new Company();
    company.setId("1");
    timesheet.setEmployee(user);
    final CompanyTaSetting companyTaSetting = new CompanyTaSetting();
    final StaticTimezone staticTimezone = new StaticTimezone();
    staticTimezone.setName("Africa/Abidjan");
    companyTaSetting.setTimeZone(staticTimezone);
    final EmployeeTimeLog employeeTimeLog = new EmployeeTimeLog();
    employeeTimeLog.setId("1");
    employeeTimeLog.setDurationMin(720);
    employeeTimeLog.setStart(Timestamp.valueOf(LocalDateTime.parse("2020-07-05T23:00:00")));
    final List<EmployeeTimeLog> employeeTimeLogs = new ArrayList<>();
    final Map<Double, Integer> overtimeMinutes = new HashMap<>();
    overtimeMinutes.put(1.5, 60);
    overtimeMinutes.put(2.0, 120);
    employeeTimeLogs.add(employeeTimeLog);
    final long startOfTimesheetWeek =
        DateUtil.getFirstHourOfWeek(
            timesheet.getTimePeriod().getStartDate(), companyTaSetting.getTimeZone().getName());
    final Timestamp timesheetEnd = timesheet.getTimePeriod().getEndDate();
    Mockito.when(timeSheetService.findTimeSheetById("1")).thenReturn(timesheet);
    Mockito.when(attendanceSettingsService.findCompanySetting()).thenReturn(companyTaSetting);
    Mockito.when(
            timeOffRequestService.findTimeOffHoursBetweenWorkPeriod(
                user, timePeriod.getStartDate().getTime(), timePeriod.getEndDate().getTime()))
        .thenReturn(1);
    Mockito.when(
            genericHoursService.findEntriesBetweenDates(
                startOfTimesheetWeek * CONVERT_SECOND_TO_MS, timesheetEnd.getTime(), "1", true))
        .thenReturn(employeeTimeLogs);
    Mockito.when(
            overtimeService.findAllOvertimeHours(Mockito.anyList(), Mockito.any(), Mockito.any()))
        .thenReturn(overtimeMinutes);
    final AttendanceSummaryDto attendanceSummaryDto =
        attendanceMyHoursService.findAttendanceSummary("1");
    assertThat(attendanceSummaryDto.getWorkedMinutes()).isEqualTo(660);
  }

  @Test
  void findUserTimeZone() {
    final Timesheet timesheet = new Timesheet();
    final User user = new User();
    user.setId("1");
    timesheet.setEmployee(user);
    final EmployeesTaSetting employeesTaSetting = new EmployeesTaSetting();
    final StaticTimezone staticTimezone = new StaticTimezone();
    user.setTimeZone(staticTimezone);
    user.getTimeZone().setName("Africa/Abidjan");
    Mockito.when(timeSheetService.findTimeSheetById("1")).thenReturn(timesheet);
    Mockito.when(userService.findById("1")).thenReturn(user);
    final String timeZone = attendanceMyHoursService.findUserTimeZone("1");
    assertThat(timeZone).isEqualTo("Africa/Abidjan");
  }

  @Test
  void findAllHours() {
    final OvertimeDetailDto overtimeDetailDto = new OvertimeDetailDto();
    overtimeDetailDto.setTimeLogId("1");
    final ArrayList<OverTimeMinutesDto> overTimeMinutesDtos = new ArrayList<>();
    final OverTimeMinutesDto overTimeMinutesDto = new OverTimeMinutesDto();
    overTimeMinutesDto.setTimeLogId("1");
    overTimeMinutesDto.setMinutes(240);
    overTimeMinutesDto.setRate(2);
    overTimeMinutesDto.setStartTime(LocalDateTime.parse("2020-07-03T09:00:00"));
    overTimeMinutesDto.setType(StaticEmployeesTaTimeType.TimeType.WORK.name());
    overTimeMinutesDtos.add(overTimeMinutesDto);
    overtimeDetailDto.setTotalMinutes(240);
    overtimeDetailDto.setOverTimeMinutesDtos(overTimeMinutesDtos);
    final Timesheet timesheet = new Timesheet();
    final User user = new User();
    final Company company = new Company();
    company.setId("1");
    user.setId("1");
    final TimePeriod timePeriod = new TimePeriod();
    timePeriod.setStartDate(Timestamp.valueOf(LocalDateTime.parse("2020-07-01T01:00:00")));
    timePeriod.setEndDate(Timestamp.valueOf(LocalDateTime.parse("2020-07-08T01:00:00")));
    timesheet.setEmployee(user);
    timesheet.setTimePeriod(timePeriod);
    final StaticTimezone staticTimezone = new StaticTimezone();
    staticTimezone.setName("Asia/Shanghai");
    final CompanyTaSetting companyTaSetting = new CompanyTaSetting();
    companyTaSetting.setTimeZone(staticTimezone);

    final EmployeeTimeEntry entry = new EmployeeTimeEntry();
    entry.setId("1");
    entry.setComment("aaa");
    final EmployeeTimeLog employeeTimeLog = new EmployeeTimeLog();
    employeeTimeLog.setId("1");
    employeeTimeLog.setDurationMin(720);
    employeeTimeLog.setEntry(entry);
    employeeTimeLog.setStart(Timestamp.valueOf(LocalDateTime.parse("2020-07-03T09:00:00")));
    final List<EmployeeTimeLog> employeeTimeLogs = new ArrayList<>();
    employeeTimeLogs.add(employeeTimeLog);

    final LocalDateEntryDto localDateEntryDto = new LocalDateEntryDto();
    localDateEntryDto.setStartTime(LocalDateTime.parse("2020-07-01T01:00:00"));
    localDateEntryDto.setEndTime(LocalDateTime.parse("2020-07-08T01:00:00"));
    localDateEntryDto.setDuration(720);
    localDateEntryDto.setTimeLogId("1");
    final LocalDate week =
        DateUtil.getFirstDayOfWeek(
            Timestamp.valueOf(LocalDateTime.parse("2020-07-01T01:00:00")), "Asia" + "/Shanghai");
    localDateEntryDto.setWeek(week);

    Mockito.when(timeSheetService.findTimeSheetById("1")).thenReturn(timesheet);
    Mockito.when(attendanceSettingsService.findCompanySetting()).thenReturn(companyTaSetting);
    Mockito.when(
            genericHoursService.findEntriesBetweenDates(
                timePeriod.getStartDate().getTime(), timePeriod.getEndDate().getTime(), "1", false))
        .thenReturn(employeeTimeLogs);
    Mockito.when(overtimeService.getLocalDateEntries(timesheet, companyTaSetting))
        .thenReturn(Collections.singletonList(localDateEntryDto));
    Mockito.when(
            overtimeService.getOvertimeEntries(
                Collections.singletonList(localDateEntryDto), timesheet, companyTaSetting))
        .thenReturn(Collections.singletonList(overtimeDetailDto));
    final List<AllTimeEntryDto> allTimeEntryDtos = attendanceMyHoursService.findAllHours("1");
    assertThat(allTimeEntryDtos.get(0).getComments()).isEqualTo("aaa");
    assertThat(
            allTimeEntryDtos.get(0).getAllTimeLogs().get(0).getOvertimeDetails().getTotalMinutes())
        .isEqualTo(240);
  }

  @Test
  void findMyHourEntry() {
    final EmployeeTimeEntry entry = new EmployeeTimeEntry();
    entry.setId("1");
    entry.setComment("aaa");
    final EmployeeTimeLog employeeTimeLog = new EmployeeTimeLog();
    employeeTimeLog.setId("1");
    employeeTimeLog.setDurationMin(60);
    employeeTimeLog.setEntry(entry);
    employeeTimeLog.setStart(Timestamp.valueOf(LocalDateTime.parse("2020-07-03T09:00:00")));
    final StaticEmployeesTaTimeType staticEmployeesTaTimeType = new StaticEmployeesTaTimeType();
    staticEmployeesTaTimeType.setName("WORK");
    employeeTimeLog.setTimeType(staticEmployeesTaTimeType);
    final List<EmployeeTimeLog> employeeTimeLogs = new ArrayList<>();
    employeeTimeLogs.add(employeeTimeLog);

    Mockito.when(employeeTimeEntryService.findById("1")).thenReturn(entry);
    Mockito.when(employeeTimeLogRepository.findAllByEntryId("1")).thenReturn(employeeTimeLogs);
    final TimeEntryDto timeEntryDto = attendanceMyHoursService.findMyHourEntry("1");
    assertThat(timeEntryDto.getHoursWorked()).isEqualTo(1);
  }

  @Test
  void submitHoursForApproval() {
    final StaticTimesheetStatus staticTimesheetStatus = new StaticTimesheetStatus();
    staticTimesheetStatus.setId("1");
    Mockito.when(staticTimesheetStatusRepository.findByName(Mockito.anyString()))
        .thenReturn(staticTimesheetStatus);
    assertThatCode(() -> attendanceMyHoursService.submitHoursForApproval("1"))
        .doesNotThrowAnyException();
  }

  @Test
  void findTimesheetStatus() {
    final Timesheet timesheet = new Timesheet();
    final StaticTimesheetStatus staticTimesheetStatus = new StaticTimesheetStatus();
    staticTimesheetStatus.setName("ACTIVE");
    timesheet.setStatus(staticTimesheetStatus);
    Mockito.when(timeSheetService.findTimeSheetById(Mockito.anyString())).thenReturn(timesheet);
    assertThatCode(() -> attendanceMyHoursService.findTimesheetStatus("1"))
        .doesNotThrowAnyException();
  }

  @Test
  void deleteMyHourEntry() {
    final User user = new User();
    user.setId("user_id");

    final EmployeeTimeEntry employeeTimeEntry = new EmployeeTimeEntry();
    employeeTimeEntry.setEmployee(user);

    final EmployeeTimeLog employeeTimeLog = new EmployeeTimeLog();
    employeeTimeLog.setStart(new Timestamp(new Date().getTime()));
    Mockito.when(employeeTimeEntryService.findById("1")).thenReturn(employeeTimeEntry);
    Mockito.when(employeeTimeLogRepository.findAllByEntryId("1"))
        .thenReturn(Arrays.asList(employeeTimeLog));
    Mockito.when(userService.findById("user_id")).thenReturn(user);
    assertThatCode(() -> attendanceMyHoursService.deleteMyHourEntry("1", "user_id"))
        .doesNotThrowAnyException();
  }

  @Test
  void findUserAttendanceEnrollInfo() {
    final TimePeriod timePeriod = new TimePeriod();
    timePeriod.setId("1");
    final Timesheet timesheet = new Timesheet();
    timesheet.setRemovedAt(Timestamp.valueOf(LocalDateTime.parse("2020-07-03T09:00:00")));
    Mockito.when(employeesTaSettingService.isEnrolledInTA("1")).thenReturn(true);
    Mockito.when(timePeriodService.findCompanyCurrentPeriod()).thenReturn(timePeriod);
    Mockito.when(timeSheetService.findActiveByPeriodAndUser("1", "1")).thenReturn(timesheet);
    assertThatCode(() -> attendanceMyHoursService.findUserAttendanceEnrollInfo("1"))
        .doesNotThrowAnyException();
  }
}
