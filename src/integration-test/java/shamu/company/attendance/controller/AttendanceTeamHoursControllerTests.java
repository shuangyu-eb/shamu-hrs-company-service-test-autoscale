package shamu.company.attendance.controller;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shamu.company.WebControllerBaseTests;
import shamu.company.attendance.dto.AttendanceTeamHoursDto;
import shamu.company.attendance.dto.EmployeeOvertimeDetailsDto;
import shamu.company.attendance.dto.TeamHoursPageInfoDto;
import shamu.company.attendance.dto.TimeAndAttendanceDetailsDto;
import shamu.company.attendance.service.AttendanceTeamHoursService;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.utils.JsonUtil;
import shamu.company.utils.UuidUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebMvcTest(controllers = AttendanceTeamHoursController.class)
class AttendanceTeamHoursControllerTests extends WebControllerBaseTests {

  @Autowired private MockMvc mockMvc;

  @MockBean private AttendanceTeamHoursService attendanceTeamHoursService;

  @MockBean private TimePeriodService timePeriodService;

  @Nested
  class findAttendanceTeamPendingHours {

    @Test
    void testFindAttendanceTeamPendingHours() throws Exception {
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

      final List<AttendanceTeamHoursDto> teamHoursDtos = new ArrayList<>();
      final AttendanceTeamHoursDto attendanceTeamHoursDto = new AttendanceTeamHoursDto();
      attendanceTeamHoursDto.setId(UuidUtil.getUuidString());
      teamHoursDtos.add(attendanceTeamHoursDto);
      final TeamHoursPageInfoDto teamHoursPageInfoDto = new TeamHoursPageInfoDto();
      given(
              attendanceTeamHoursService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                  Mockito.any(),
                  Mockito.any(),
                  Mockito.any(),
                  Mockito.any(),
                  Mockito.any(),
                  Mockito.any()))
          .willReturn(teamHoursPageInfoDto);

      final MvcResult response =
          mockMvc
              .perform(
                  MockMvcRequestBuilders.get(
                          "/company/time-and-attendance/team-hours/pending-hours/1/team_hours?page=1")
                      .contentType(MediaType.APPLICATION_JSON)
                      .headers(httpHeaders))
              .andReturn();

      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }
  }

  @Nested
  class findAttendanceTeamApprovedHours {

    @Test
    void testFindAttendanceTeamApprovedHours() throws Exception {
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

      final List<AttendanceTeamHoursDto> teamHoursDtos = new ArrayList<>();
      final AttendanceTeamHoursDto attendanceTeamHoursDto = new AttendanceTeamHoursDto();
      attendanceTeamHoursDto.setId(UuidUtil.getUuidString());
      teamHoursDtos.add(attendanceTeamHoursDto);
      final TeamHoursPageInfoDto teamHoursPageInfoDto = new TeamHoursPageInfoDto();
      given(
              attendanceTeamHoursService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
                  Mockito.any(),
                  Mockito.any(),
                  Mockito.any(),
                  Mockito.any(),
                  Mockito.any(),
                  Mockito.any()))
          .willReturn(teamHoursPageInfoDto);

      final MvcResult response =
          mockMvc
              .perform(
                  MockMvcRequestBuilders.get(
                          "/company/time-and-attendance/team-hours/approved-hours/1/team_hours?page=1")
                      .contentType(MediaType.APPLICATION_JSON)
                      .headers(httpHeaders))
              .andReturn();

      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }
  }

  @Test
  void testFindTeamHoursSummary() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/time-and-attendance/team-hours-summary/1/team_hours")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindTimePeriodsByCompany() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-and-attendance/time-periods")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void approvePendingHours() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final Set<String> selectedTimesheetIds = new HashSet<>();
    selectedTimesheetIds.add(UuidUtil.getUuidString());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch(
                        "/company/time-and-attendance/team-hours/pending-hours/approved")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(selectedTimesheetIds))
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void findEmployeeInfo() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/time-and-attendance/team-hours/employee-info/1")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void findApprovalDaysBeforePayroll() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/time-and-attendance/approval-days-before-payroll")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void findAttendanceDetails() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-and-attendance/attendance-details")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void updateAttendanceDetails() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final TimeAndAttendanceDetailsDto timeAndAttendanceDetailsDto =
        new TimeAndAttendanceDetailsDto();
    final List<EmployeeOvertimeDetailsDto> overtimeDetails = new ArrayList<>();
    final EmployeeOvertimeDetailsDto employeeOvertimeDetailsDto = new EmployeeOvertimeDetailsDto();
    overtimeDetails.add(employeeOvertimeDetailsDto);
    final List<String> userIds = new ArrayList<>();
    userIds.add("1");
    timeAndAttendanceDetailsDto.setOvertimeDetails(overtimeDetails);
    timeAndAttendanceDetailsDto.setRemovedUserIds(userIds);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/time-and-attendance/details")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(timeAndAttendanceDetailsDto))
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void findAttendancePendingCount() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-and-attendance/pending-count")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
