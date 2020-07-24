package shamu.company.attendance.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;
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
import shamu.company.attendance.dto.TeamHoursPageInfoDto;
import shamu.company.attendance.service.AttendanceTeamHoursService;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.utils.UuidUtil;

@WebMvcTest(controllers = AttendanceTeamHoursController.class)
class AttendanceTeamHoursControllerTests extends WebControllerBaseTests {

  @Autowired private MockMvc mockMvc;

  @MockBean private AttendanceTeamHoursService attendanceTeamHoursService;

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
                  Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
          .willReturn(teamHoursPageInfoDto);

      final MvcResult response =
          mockMvc
              .perform(
                  MockMvcRequestBuilders.get(
                          "/company/time-and-attendance/team-hours/pending-hours/1?page=1")
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
                  Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
          .willReturn(teamHoursPageInfoDto);

      final MvcResult response =
          mockMvc
              .perform(
                  MockMvcRequestBuilders.get(
                          "/company/time-and-attendance/team-hours/approved-hours/1?page=1")
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
                        "/company/time-and-attendance/team-hours/total-time-off/1")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
