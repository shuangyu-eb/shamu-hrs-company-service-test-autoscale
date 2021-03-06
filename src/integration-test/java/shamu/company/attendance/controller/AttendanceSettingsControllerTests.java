package shamu.company.attendance.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shamu.company.WebControllerBaseTests;
import shamu.company.attendance.dto.CompanyTaSettingsDto;
import shamu.company.attendance.dto.EmployeesTaSettingDto;
import shamu.company.attendance.dto.OvertimePolicyDto;
import shamu.company.authorization.Permission;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.utils.JsonUtil;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = AttendanceSettingsController.class)
public class AttendanceSettingsControllerTests extends WebControllerBaseTests {

  @Autowired private MockMvc mockMvc;

  @Test
  void testFindCompanySettings() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    setPermission(Permission.Name.MANAGE_COMPANY_USER.name());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-and-attendance/company-settings")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindEmployeeSettings() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    setPermission(Permission.Name.VIEW_SELF.name());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-and-attendance/employee-settings/1")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindAllTimezones() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-and-attendance/static-timezones")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateCompanySettings() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    setPermission(Permission.Name.MANAGE_COMPANY_USER.name());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/time-and-attendance/company-settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(new CompanyTaSettingsDto()))
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void updateEmployeeSettings() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    setPermission(Permission.Name.EDIT_SELF.name());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/time-and-attendance/1/employee-settings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(new EmployeesTaSettingDto()))
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void isInAttendance() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-and-attendance/1/is-in-attendance")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void createOvertimePolicy() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    setPermission(Permission.Name.MANAGE_COMPANY_USER.name());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/time-and-attendance/create-overtime-policy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(new OvertimePolicyDto()))
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void deleteOvertimePolicy() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    setPermission(Permission.Name.MANAGE_COMPANY_USER.name());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.delete(
                        "/company/time-and-attendance/delete-overtime-policy/1")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void updateOvertimePolicy() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    setPermission(Permission.Name.MANAGE_COMPANY_USER.name());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/time-and-attendance/overtime-policy")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(new OvertimePolicyDto()))
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void getOvertimePolicies() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    setPermission(Permission.Name.MANAGE_COMPANY_USER.name());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-and-attendance/overtime-policies")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void getOvertimePolicyDetail() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    setPermission(Permission.Name.MANAGE_COMPANY_USER.name());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-and-attendance/overtime-policies/1")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void findAllPolicyNames() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    setPermission(Permission.Name.MANAGE_COMPANY_USER.name());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-and-attendance/all-active-policy-name")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindTeamHoursSummary() throws Exception {
    setPermission(Permission.Name.MANAGE_TEAM_USER.name());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-and-attendance/overtime-alert-minutes")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
