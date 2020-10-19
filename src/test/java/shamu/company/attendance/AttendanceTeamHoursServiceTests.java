package shamu.company.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import shamu.company.attendance.dto.AttendanceSummaryDto;
import shamu.company.attendance.dto.AttendanceTeamHoursDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.Timesheet;
import shamu.company.attendance.service.AttendanceMyHoursService;
import shamu.company.attendance.service.AttendanceSettingsService;
import shamu.company.attendance.service.AttendanceTeamHoursService;
import shamu.company.attendance.service.OvertimeService;
import shamu.company.attendance.service.StaticTimesheetStatusService;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.common.entity.PayrollDetail;
import shamu.company.common.service.PayrollDetailService;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.UserService;
import shamu.company.utils.UuidUtil;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class AttendanceTeamHoursServiceTests {

  @InjectMocks private AttendanceTeamHoursService attendanceTeamHoursService;

  @Mock private TimeSheetService timeSheetService;

  @Mock private AttendanceMyHoursService attendanceMyHoursService;

  @Mock private AttendanceSettingsService attendanceSettingsService;

  @Mock private OvertimeService overtimeService;

  @Mock private TimeOffRequestService timeOffRequestService;

  @Mock private StaticTimesheetStatusService staticTimesheetStatusService;

  @Mock private TimePeriodService timePeriodService;

  @Mock private PayrollDetailService payrollDetailService;

  @Mock private UserService userService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class findTeamTimeSheetsByIdAndCompanyIdAndStatus {

    List<AttendanceTeamHoursDto> attendanceTeamHoursDtos;
    List<Timesheet> timesheets;
    Timesheet timesheet;
    StaticTimesheetStatus staticTimesheetStatus;
    String timesheetId;
    String companyId;
    TimeSheetStatus status;
    CompanyTaSetting companyTaSetting;
    List<EmployeeTimeLog> workedMinutes;
    int workedMin;
    Map<Double, Integer> overtimeMinutes;
    User user;
    UserPersonalInformation userPersonalInformation;
    Page<Timesheet> page;
    Pageable pageable;

    @BeforeEach
    void init() {
      staticTimesheetStatus = new StaticTimesheetStatus();
      staticTimesheetStatus.setName("1");
      attendanceTeamHoursDtos = new ArrayList<>();
      timesheets = new ArrayList<>();
      timesheet = new Timesheet();
      timesheet.setStatus(staticTimesheetStatus);
      timesheets.add(timesheet);
      timesheetId = UuidUtil.getUuidString();
      companyId = UuidUtil.getUuidString();
      status = TimeSheetStatus.APPROVED;
      companyTaSetting = new CompanyTaSetting();
      workedMinutes = new ArrayList<>();
      workedMin = 1000;
      overtimeMinutes = new HashMap<>();
      overtimeMinutes.put(1.2, 12);
      user = new User(UuidUtil.getUuidString());
      userPersonalInformation = new UserPersonalInformation();
      userPersonalInformation.setFirstName("pi");
      userPersonalInformation.setLastName("pi");
      user.setUserPersonalInformation(userPersonalInformation);
      timesheet.setEmployee(user);
      pageable = PageRequest.of(0, 2);
      page = new PageImpl<>(timesheets, pageable, timesheets.size());
    }

    @Test
    void whenExistTimeSheetAndIsTeamHourType_thenShouldReturnDtos() {
      timesheet.setId(UuidUtil.getUuidString());

      Mockito.when(
              timeSheetService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                  timesheetId, Collections.singletonList(status.getValue()), "1", pageable))
          .thenReturn(page);
      Mockito.when(attendanceSettingsService.findCompanySetting()).thenReturn(companyTaSetting);
      Mockito.when(attendanceMyHoursService.findAllRelevantTimelogs(timesheet, companyTaSetting))
          .thenReturn(workedMinutes);
      Mockito.when(attendanceMyHoursService.getTotalNumberOfWorkedMinutes(workedMinutes, timesheet))
          .thenReturn(workedMin);
      Mockito.when(overtimeService.findAllOvertimeHours(workedMinutes, timesheet, companyTaSetting))
          .thenReturn(overtimeMinutes);

      assertThat(
              attendanceTeamHoursService
                  .findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                      timesheetId,
                      "team_hours",
                      Collections.singletonList(status.getValue()),
                      "1",
                      pageable)
                  .getContent()
                  .get(0)
                  .getWorkedMinutes())
          .isEqualTo(workedMin);
      assertThat(
              attendanceTeamHoursService
                  .findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                      timesheetId,
                      "team_hours",
                      Collections.singletonList(status.getValue()),
                      "1",
                      pageable)
                  .getContent()
                  .get(0)
                  .getOverTimeMinutes())
          .isEqualTo(overtimeMinutes.get(1.2));
      assertThatCode(
              () ->
                  attendanceTeamHoursService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                      timesheetId,
                      "team_hours",
                      Collections.singletonList(status.getValue()),
                      "1",
                      pageable))
          .doesNotThrowAnyException();
    }

    @Test
    void whenExistTimeSheetAndIsCompanyHourType_thenShouldReturnDtos() {
      timesheet.setId(UuidUtil.getUuidString());

      Mockito.when(
              timeSheetService.findCompanyTimeSheetsByIdAndStatus(
                  timesheetId, Collections.singletonList(status.getValue()), pageable))
          .thenReturn(page);
      Mockito.when(attendanceSettingsService.findCompanySetting()).thenReturn(companyTaSetting);
      Mockito.when(attendanceMyHoursService.findAllRelevantTimelogs(timesheet, companyTaSetting))
          .thenReturn(workedMinutes);
      Mockito.when(attendanceMyHoursService.getTotalNumberOfWorkedMinutes(workedMinutes, timesheet))
          .thenReturn(workedMin);
      Mockito.when(overtimeService.findAllOvertimeHours(workedMinutes, timesheet, companyTaSetting))
          .thenReturn(overtimeMinutes);

      assertThat(
              attendanceTeamHoursService
                  .findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                      timesheetId,
                      "company_hours",
                      Collections.singletonList(status.getValue()),
                      "1",
                      pageable)
                  .getContent()
                  .get(0)
                  .getWorkedMinutes())
          .isEqualTo(workedMin);
      assertThat(
              attendanceTeamHoursService
                  .findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                      timesheetId,
                      "company_hours",
                      Collections.singletonList(status.getValue()),
                      "1",
                      pageable)
                  .getContent()
                  .get(0)
                  .getOverTimeMinutes())
          .isEqualTo(overtimeMinutes.get(1.2));
      assertThatCode(
              () ->
                  attendanceTeamHoursService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                      timesheetId,
                      "company_hours",
                      Collections.singletonList(status.getValue()),
                      "1",
                      pageable))
          .doesNotThrowAnyException();
    }
  }

  @Nested
  class testFindTeamHoursSummary {
    List<Timesheet> timesheets;
    Timesheet timesheet;
    User user;
    TimePeriod timePeriod;
    CompanyTaSetting companyTaSetting;
    List<EmployeeTimeLog> employeeTimeLogs;
    UserCompensation userCompensation;
    CompensationFrequency compensationFrequency;
    Map<Double, Integer> overtimeMinutes;

    @BeforeEach
    void init() {
      timesheets = new ArrayList<>();
      timesheet = new Timesheet();
      user = new User();
      timePeriod = new TimePeriod();
      companyTaSetting = new CompanyTaSetting();
      employeeTimeLogs = new ArrayList<>();
      userCompensation = new UserCompensation();
      compensationFrequency = new CompensationFrequency();
      overtimeMinutes = new HashMap<>();
      overtimeMinutes.put(1.5, 60);
      overtimeMinutes.put(2.0, 120);
      timePeriod.setStartDate(Timestamp.valueOf(LocalDateTime.parse("2020-07-01T01:00:00")));
      timePeriod.setEndDate(Timestamp.valueOf(LocalDateTime.parse("2020-07-08T01:00:00")));
      timesheet.setEmployee(user);
      timesheet.setTimePeriod(timePeriod);
      timesheets.add(timesheet);
      compensationFrequency.setName("Per Hour");
      userCompensation.setCompensationFrequency(compensationFrequency);
      timesheet.setUserCompensation(userCompensation);
      Mockito.when(attendanceSettingsService.findCompanySetting()).thenReturn(companyTaSetting);
      Mockito.when(
              timeOffRequestService.findTimeOffHoursBetweenWorkPeriod(
                  user, timePeriod.getStartDate().getTime(), timePeriod.getEndDate().getTime()))
          .thenReturn(20);
      Mockito.when(attendanceMyHoursService.findAllRelevantTimelogs(Mockito.any(), Mockito.any()))
          .thenReturn(employeeTimeLogs);

      Mockito.when(
              attendanceMyHoursService.getTotalNumberOfWorkedMinutes(
                  Mockito.anyList(), Mockito.any()))
          .thenReturn(200);
      Mockito.when(
              overtimeService.findAllOvertimeHours(Mockito.anyList(), Mockito.any(), Mockito.any()))
          .thenReturn(overtimeMinutes);
    }

    @Test
    void whenIsTeamHourType() {
      Mockito.when(
              timeSheetService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                  Mockito.anyString(), Mockito.anyString(), Mockito.anyList()))
          .thenReturn(timesheets);
      final AttendanceSummaryDto attendanceSummaryDto =
          attendanceTeamHoursService.findTeamHoursSummary(
              "1", Collections.singletonList("1"), "1", "team_hours");
      assertThat(attendanceSummaryDto.getOverTimeMinutes()).isEqualTo(180);
      assertThat(attendanceSummaryDto.getWorkedMinutes()).isEqualTo(20);
      assertThat(attendanceSummaryDto.getTotalPtoMinutes()).isEqualTo(1200);
    }

    @Test
    void whenIsCompanyHourType() {
      Mockito.when(
              timeSheetService.findCompanyTimeSheetsByIdAndStatus(
                  Mockito.anyString(), Mockito.anyList()))
          .thenReturn(timesheets);
      final AttendanceSummaryDto attendanceSummaryDto =
          attendanceTeamHoursService.findTeamHoursSummary(
              "1", Collections.singletonList("1"), "1", "company_hours");
      assertThat(attendanceSummaryDto.getOverTimeMinutes()).isEqualTo(180);
      assertThat(attendanceSummaryDto.getWorkedMinutes()).isEqualTo(20);
      assertThat(attendanceSummaryDto.getTotalPtoMinutes()).isEqualTo(1200);
    }
  }

  @Test
  void approvePendingHours() {
    final Set<String> selectedTimesheetIds = new HashSet<>();
    final String timesheetId = UuidUtil.getUuidString();
    selectedTimesheetIds.add(timesheetId);
    final StaticTimesheetStatus staticTimesheetStatus = new StaticTimesheetStatus();
    staticTimesheetStatus.setName(TimeSheetStatus.APPROVED.name());
    final Timesheet timesheet = new Timesheet();
    timesheet.setId(timesheetId);
    final List<Timesheet> timesheets = new ArrayList<>();
    timesheets.add(timesheet);

    Mockito.when(staticTimesheetStatusService.findByName(TimeSheetStatus.APPROVED.name()))
        .thenReturn(staticTimesheetStatus);
    Mockito.when(timeSheetService.findTimeSheetById(Mockito.anyString())).thenReturn(timesheet);
    Mockito.when(timeSheetService.findAllById(selectedTimesheetIds)).thenReturn(timesheets);
    Mockito.when(userService.findById("1")).thenReturn(new User());
    assertThatCode(() -> attendanceTeamHoursService.approvePendingHours(selectedTimesheetIds, "1"))
        .doesNotThrowAnyException();
  }

  @Nested
  class TestFindEmployeeSummary {
    String timePeriodId;
    String companyId;
    String userId;
    String hourType;
    List<String> statusList;
    CompanyTaSetting companyTaSetting = new CompanyTaSetting();
    List<Timesheet> timesheets = new ArrayList<>();
    Timesheet timesheet = new Timesheet();

    @BeforeEach
    void init() {
      companyId = "test_company_id";
      hourType = "team_hours";
      userId = "test_user_id";
      timePeriodId = "test_period_id";
      statusList =
          Arrays.asList(
              StaticTimesheetStatus.TimeSheetStatus.SUBMITTED.name(),
              StaticTimesheetStatus.TimeSheetStatus.APPROVED.name());

      final User user = new User();
      final UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
      userPersonalInformation.setFirstName("first_name");
      userPersonalInformation.setLastName("last_name");
      user.setUserPersonalInformation(userPersonalInformation);
      timesheet.setEmployee(user);

      final TimePeriod timePeriod = new TimePeriod();
      timePeriod.setStartDate(new Timestamp(new Date().getTime()));
      timePeriod.setEndDate(new Timestamp(new Date().getTime()));
      timesheet.setTimePeriod(timePeriod);

      final StaticTimesheetStatus timesheetStatus = new StaticTimesheetStatus();
      timesheetStatus.setName("status");
      timesheet.setStatus(timesheetStatus);
      timesheets.add(timesheet);
    }

    @Test
    void whenValidCompany_shouldSucceed() {
      Mockito.when(attendanceSettingsService.findCompanySetting()).thenReturn(companyTaSetting);
      Mockito.when(
              timeSheetService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                  userId, timePeriodId, statusList))
          .thenReturn(timesheets);

      final List<EmployeeTimeLog> workedMinutes = new ArrayList<>();
      Mockito.when(attendanceMyHoursService.findAllRelevantTimelogs(timesheet, companyTaSetting))
          .thenReturn(workedMinutes);
      Mockito.when(overtimeService.findAllOvertimeHours(workedMinutes, timesheet, companyTaSetting))
          .thenReturn(new HashMap<>());
      Mockito.when(attendanceMyHoursService.getTotalNumberOfWorkedMinutes(workedMinutes, timesheet))
          .thenReturn(10);
      assertThatCode(
              () ->
                  attendanceTeamHoursService.findEmployeeAttendanceSummary(
                      timePeriodId, userId, hourType))
          .doesNotThrowAnyException();
    }
  }

  @Test
  void findAttendanceDetails() {
    final CompanyTaSetting companyTaSetting = new CompanyTaSetting();
    final PayrollDetail payrollDetail = new PayrollDetail();
    final StaticCompanyPayFrequencyType payFrequencyType = new StaticCompanyPayFrequencyType();
    payFrequencyType.setName(StaticCompanyPayFrequencyType.PayFrequencyType.MONTHLY.name());
    payrollDetail.setPayFrequencyType(payFrequencyType);
    payrollDetail.setLastPayrollPayday(
        Timestamp.valueOf(LocalDateTime.parse("2020-07-07T00:00:00")));
    final TimePeriod timePeriod = new TimePeriod();
    timePeriod.setStartDate(Timestamp.valueOf(LocalDateTime.parse("2020-07-01T00:00:00")));
    timePeriod.setEndDate(Timestamp.valueOf(LocalDateTime.parse("2020-07-07T00:00:00")));
    final StaticTimezone timezone = new StaticTimezone();
    timezone.setName("Hongkong");
    companyTaSetting.setTimeZone(timezone);

    Mockito.when(attendanceSettingsService.findCompanySetting()).thenReturn(companyTaSetting);
    Mockito.when(payrollDetailService.find()).thenReturn(payrollDetail);
    Mockito.when(timePeriodService.findCompanyCurrentPeriod()).thenReturn(timePeriod);
    assertThatCode(() -> attendanceTeamHoursService.findAttendanceDetails())
        .doesNotThrowAnyException();
  }

  @Test
  void findAttendancePendingCount() {
    final TimePeriod timePeriod = new TimePeriod();
    timePeriod.setId("1");
    Mockito.when(timePeriodService.findCompanyCurrentPeriod()).thenReturn(timePeriod);
    Mockito.when(timeSheetService.findTeamHoursPendingCount(Mockito.any(), Mockito.anyString()))
        .thenReturn(1);
    Mockito.when(timeSheetService.findCompanyHoursPendingCount(Mockito.any())).thenReturn(1);
    assertThatCode(() -> attendanceTeamHoursService.findAttendancePendingCount("1"))
        .doesNotThrowAnyException();
  }

  @Nested
  class completePeriod {
    StaticTimesheetStatus completeStatus;
    StaticTimesheetStatus approvedStatus;

    @BeforeEach
    void init() {
      completeStatus = new StaticTimesheetStatus();
      completeStatus.setId("1");
      approvedStatus = new StaticTimesheetStatus();
      approvedStatus.setId("1");
      Mockito.when(staticTimesheetStatusService.findByName(TimeSheetStatus.COMPLETE.name()))
          .thenReturn(completeStatus);
      Mockito.when(staticTimesheetStatusService.findByName(TimeSheetStatus.APPROVED.name()))
          .thenReturn(approvedStatus);
    }

    @Test
    void whenTypeIsCompanyHour() {
      assertThatCode(() -> attendanceTeamHoursService.completePeriod("1", "company_hours", "1"))
          .doesNotThrowAnyException();
    }

    @Test
    void whenTypeIsTeamHour() {
      assertThatCode(() -> attendanceTeamHoursService.completePeriod("1", "team_hours", "1"))
          .doesNotThrowAnyException();
    }
  }
}
