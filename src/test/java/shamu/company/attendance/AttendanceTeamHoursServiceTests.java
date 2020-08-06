package shamu.company.attendance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.service.AttendanceMyHoursService;
import shamu.company.attendance.service.AttendanceSettingsService;
import shamu.company.attendance.service.AttendanceTeamHoursService;
import shamu.company.attendance.service.OvertimeService;
import shamu.company.attendance.service.StaticTimesheetStatusService;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.utils.UuidUtil;

class AttendanceTeamHoursServiceTests {

  @InjectMocks private AttendanceTeamHoursService attendanceTeamHoursService;

  @Mock private TimeSheetService timeSheetService;

  @Mock private AttendanceMyHoursService attendanceMyHoursService;

  @Mock private AttendanceSettingsService attendanceSettingsService;

  @Mock private OvertimeService overtimeService;

  @Mock private TimeOffRequestService timeOffRequestService;

  @Mock private StaticTimesheetStatusService staticTimesheetStatusService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class findTeamTimeSheetsByIdAndCompanyIdAndStatus {

    List<AttendanceTeamHoursDto> attendanceTeamHoursDtos;
    List<TimeSheet> timeSheets;
    TimeSheet timeSheet;
    String timesheetId;
    String companyId;
    TimeSheetStatus status;
    CompanyTaSetting companyTaSetting;
    List<EmployeeTimeLog> workedMinutes;
    int workedMin;
    Map<Double, Integer> overtimeMinutes;
    User user;
    UserPersonalInformation userPersonalInformation;
    Page<TimeSheet> page;
    Pageable pageable;

    @BeforeEach
    void init() {
      attendanceTeamHoursDtos = new ArrayList<>();
      timeSheets = new ArrayList<>();
      timeSheet = new TimeSheet();
      timeSheets.add(timeSheet);
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
      timeSheet.setEmployee(user);
      pageable = PageRequest.of(0, 2);
      page = new PageImpl<>(timeSheets, pageable, timeSheets.size());
    }

    @Test
    void whenExistTimeSheet_thenShouldReturnDtos() {
      timeSheet.setId(UuidUtil.getUuidString());

      Mockito.when(
              timeSheetService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                  timesheetId, companyId, status, "1", pageable))
          .thenReturn(page);
      Mockito.when(attendanceSettingsService.findCompanySettings(companyId))
          .thenReturn(companyTaSetting);
      Mockito.when(attendanceMyHoursService.findAllRelevantTimelogs(timeSheet, companyTaSetting))
          .thenReturn(workedMinutes);
      Mockito.when(attendanceMyHoursService.getTotalNumberOfWorkedMinutes(workedMinutes, timeSheet))
          .thenReturn(workedMin);
      Mockito.when(overtimeService.findAllOvertimeHours(workedMinutes, timeSheet, companyTaSetting))
          .thenReturn(overtimeMinutes);

      assertThat(
              attendanceTeamHoursService
                  .findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                      timesheetId, companyId, status, "1", pageable)
                  .getContent()
                  .get(0)
                  .getWorkedMinutes())
          .isEqualTo(workedMin);
      assertThat(
              attendanceTeamHoursService
                  .findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                      timesheetId, companyId, status, "1", pageable)
                  .getContent()
                  .get(0)
                  .getOverTimeMinutes())
          .isEqualTo(overtimeMinutes.get(1.2));
      assertThatCode(
              () ->
                  attendanceTeamHoursService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                      timesheetId, companyId, status, "1", pageable))
          .doesNotThrowAnyException();
    }
  }

  @Test
  void testFindTeamHoursSummary() {
    final List<TimeSheet> timeSheets = new ArrayList<>();
    final TimeSheet timeSheet = new TimeSheet();
    final User user = new User();
    final TimePeriod timePeriod = new TimePeriod();
    timePeriod.setStartDate(Timestamp.valueOf(LocalDateTime.parse("2020-07-01T01:00:00")));
    timePeriod.setEndDate(Timestamp.valueOf(LocalDateTime.parse("2020-07-08T01:00:00")));
    timeSheet.setEmployee(user);
    timeSheet.setTimePeriod(timePeriod);
    timeSheets.add(timeSheet);
    final CompanyTaSetting companyTaSetting = new CompanyTaSetting();
    final List<EmployeeTimeLog> employeeTimeLogs = new ArrayList<>();

    final UserCompensation userCompensation = new UserCompensation();
    final CompensationFrequency compensationFrequency = new CompensationFrequency();
    compensationFrequency.setName("Per Hour");
    userCompensation.setCompensationFrequency(compensationFrequency);
    timeSheet.setUserCompensation(userCompensation);

    Mockito.when(
            timeSheetService.findTimeSheetsByIdAndCompanyIdAndStatus(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyList()))
        .thenReturn(timeSheets);
    Mockito.when(attendanceSettingsService.findCompanySettings(Mockito.anyString()))
        .thenReturn(companyTaSetting);
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

    final Map<Double, Integer> overtimeMinutes = new HashMap<>();
    overtimeMinutes.put(1.5, 60);
    overtimeMinutes.put(2.0, 120);
    Mockito.when(
            overtimeService.findAllOvertimeHours(Mockito.anyList(), Mockito.any(), Mockito.any()))
        .thenReturn(overtimeMinutes);
    final AttendanceSummaryDto attendanceSummaryDto =
        attendanceTeamHoursService.findTeamHoursSummary("1", "1", "1");
    assertThat(attendanceSummaryDto.getOverTimeMinutes()).isEqualTo(180);
    assertThat(attendanceSummaryDto.getWorkedMinutes()).isEqualTo(20);
    assertThat(attendanceSummaryDto.getTotalPtoMinutes()).isEqualTo(1200);
  }

  @Test
  void approvePendingHours() {
    Set<String> selectedTimesheetIds = new HashSet<>();
    final String timesheetId = UuidUtil.getUuidString();
    selectedTimesheetIds.add(timesheetId);
    final StaticTimesheetStatus staticTimesheetStatus = new StaticTimesheetStatus();
    staticTimesheetStatus.setName(TimeSheetStatus.APPROVED.name());
    final TimeSheet timeSheet = new TimeSheet();
    timeSheet.setId(timesheetId);
    final List<TimeSheet> timeSheets = new ArrayList<>();
    timeSheets.add(timeSheet);

    Mockito.when(staticTimesheetStatusService.findByName(TimeSheetStatus.APPROVED.name()))
        .thenReturn(staticTimesheetStatus);
    Mockito.when(timeSheetService.findTimeSheetById(Mockito.anyString())).thenReturn(timeSheet);
    Mockito.when(timeSheetService.findAllById(selectedTimesheetIds)).thenReturn(timeSheets);

    assertThatCode(() -> attendanceTeamHoursService.approvePendingHours(selectedTimesheetIds))
        .doesNotThrowAnyException();
  }
}
