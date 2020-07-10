package shamu.company.attendance.controller;

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
import shamu.company.attendance.dto.TimeEntryDto;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.utils.JsonUtil;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = AttendanceMyHoursController.class)
public class AttendanceMyHoursControllerTests extends WebControllerBaseTests {
  @MockBean private TimePeriodService timePeriodService;

  @Autowired private MockMvc mockMvc;

  @Test
  void findIsAttendanceSetUp() throws Exception {
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/time-and-attendance/1/add-time-entries")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(new TimeEntryDto()))
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    Mockito.verify(attendanceMyHoursService, Mockito.times(1))
        .saveTimeEntry(Mockito.anyString(), Mockito.any());
  }

  @Test
  void findMyHoursList() throws Exception {
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-and-attendance/total-paid-time/1")
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    Mockito.verify(attendanceMyHoursService, Mockito.times(1))
        .findMyHoursEntries(Mockito.anyString());
  }

  @Test
  void findUserTimeZone() throws Exception {
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-and-attendance/user-timezone/1")
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    Mockito.verify(attendanceMyHoursService, Mockito.times(1))
        .findUserTimeZone(Mockito.anyString());
  }

  @Test
  void testGetAllPayPeriodFrequency() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-and-attendance/time-periods/1")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
