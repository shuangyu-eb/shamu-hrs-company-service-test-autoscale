package shamu.company.attendance.service;

import static org.assertj.core.api.Assertions.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import shamu.company.attendance.dto.AttendanceTeamHoursDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.utils.UuidUtil;

class AttendanceTeamHoursServiceTests {

  @InjectMocks private AttendanceTeamHoursService attendanceTeamHoursService;

  @Mock private TimeSheetService timeSheetService;

  @Mock private AttendanceMyHoursService attendanceMyHoursService;

  @Mock private AttendanceSettingsService attendanceSettingsService;

  @Mock private OvertimeService overtimeService;

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
                  timesheetId, companyId, status, pageable))
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
                  .findTeamTimeSheetsByIdAndCompanyIdAndStatus(timesheetId, companyId, status, pageable)
                  .getContent().get(0)
                  .getWorkedMinutes())
          .isEqualTo(workedMin);
      assertThat(
              attendanceTeamHoursService
                  .findTeamTimeSheetsByIdAndCompanyIdAndStatus(timesheetId, companyId, status, pageable)
                  .getContent().get(0)
                  .getOverTimeMinutes())
          .isEqualTo(overtimeMinutes.get(1.2));
      assertThatCode(
              () ->
                  attendanceTeamHoursService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                      timesheetId, companyId, status, pageable))
          .doesNotThrowAnyException();
    }
  }
}
