package shamu.company.timeoff.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shamu.company.WebControllerBaseTests;
import shamu.company.authorization.Permission.Name;
import shamu.company.company.entity.Company;
import shamu.company.job.dto.JobUserDto;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.timeoff.dto.PaidHolidayDto;
import shamu.company.timeoff.dto.PaidHolidayEmployeeDto;
import shamu.company.timeoff.dto.PaidHolidayRelatedUserListDto;
import shamu.company.timeoff.entity.PaidHoliday;
import shamu.company.user.entity.User;
import shamu.company.utils.JsonUtil;

@WebMvcTest(controllers = PaidHolidayRestController.class)
class PaidHolidayRestControllerTests extends WebControllerBaseTests {

  @Autowired private MockMvc mockMvc;

  @Test
  void testGetAllPaidHolidays() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/paid-holidays").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetUserAllPaidHolidays() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/paid-holidays/user/" + getAuthUser().getId() + "")
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetPaidHolidays() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/paid-holidays/employees").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetPaidHolidaysEmployeesCount() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final PaidHolidayRelatedUserListDto paidHolidayRelatedUserListDto =
        new PaidHolidayRelatedUserListDto();
    final List<JobUserDto> jobUserDtoList = new ArrayList<>();
    paidHolidayRelatedUserListDto.setPaidHolidaySelectedEmployees(jobUserDtoList);
    given(paidHolidayService.getPaidHolidayEmployees(Mockito.any()))
        .willReturn(paidHolidayRelatedUserListDto);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/paid-holidays/employees/count")
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdatePaidHolidayEmployees() throws Exception {
    setPermission(Name.EDIT_USER.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final List<PaidHolidayEmployeeDto> paidHolidayEmployeeDtos = new ArrayList<>();
    final List<JobUserDto> jobUserDtoList = new ArrayList();
    jobUserDtoList.add(new JobUserDto());
    final PaidHolidayRelatedUserListDto paidHolidayRelatedUserListDto =
        new PaidHolidayRelatedUserListDto();
    paidHolidayRelatedUserListDto.setPaidHolidaySelectedEmployees(jobUserDtoList);
    given(paidHolidayService.getPaidHolidayEmployees(Mockito.any()))
        .willReturn(paidHolidayRelatedUserListDto);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/paid-holidays/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(paidHolidayEmployeeDtos))
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetPaidHolidaysByYear() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/paid-holidays/years/2020")
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateHolidaySelects() throws Exception {
    setPermission(Name.EDIT_PAID_HOLIDAY.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final List<PaidHolidayDto> paidHolidayDtos = new ArrayList<>();

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/paid-holidays/select")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(paidHolidayDtos))
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testCreatePaidHoliday() throws Exception {
    setPermission(Name.EDIT_PAID_HOLIDAY.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/paid-holidays")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(new PaidHolidayDto()))
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdatePaidHolidays() throws Exception {
    setPermission(Name.EDIT_PAID_HOLIDAY.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/paid-holidays/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(new PaidHolidayDto()))
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdatePaidHoliday() throws Exception {
    setPermission(Name.DELETE_PAID_HOLIDAY.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final PaidHoliday paidHoliday = new PaidHoliday();
    paidHoliday.setCompany(new Company(getAuthUser().getCompanyId()));
    paidHoliday.setCreator(new User(getAuthUser().getId()));
    given(paidHolidayService.getPaidHoliday(Mockito.any())).willReturn(paidHoliday);

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.delete("/company/paid-holidays/1").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
