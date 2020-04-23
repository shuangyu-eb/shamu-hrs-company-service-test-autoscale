package shamu.company.timeoff.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;
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
import shamu.company.authorization.Permission.Name;
import shamu.company.company.entity.Company;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserFrontendDto;
import shamu.company.timeoff.dto.TimeOffPolicyWrapperDto;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.service.TimeOffDetailService;
import shamu.company.user.entity.User;
import shamu.company.utils.JsonUtil;

@WebMvcTest(controllers = TimeOffPolicyRestController.class)
class TimeOffPolicyRestControllerTests extends WebControllerBaseTests {

  @MockBean private TimeOffDetailService timeOffDetailService;

  @Autowired private MockMvc mockMvc;

  @Test
  void testCreateTimeOffPolicy() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_POLICY.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/time-off-policies")
                    .headers(httpHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(new TimeOffPolicyWrapperDto())))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateTimeOffPolicy() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_POLICY.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final List<TimeOffPolicyUserFrontendDto> userStartBalances = new ArrayList<>();
    final TimeOffPolicyUserFrontendDto timeOffPolicyUserFrontendDto = new TimeOffPolicyUserFrontendDto();
    timeOffPolicyUserFrontendDto.setUserId("1");
    userStartBalances.add(timeOffPolicyUserFrontendDto);
    final TimeOffPolicyWrapperDto timeOffPolicyWrapperDto = new TimeOffPolicyWrapperDto();
    timeOffPolicyWrapperDto.setUserStartBalances(userStartBalances);

    final User targetUser = new User();
    targetUser.setCompany(new Company(getAuthUser().getCompanyId()));
    given(userService.findById("1")).willReturn(targetUser);
    final TimeOffPolicy timeOffPolicy = new TimeOffPolicy();
    timeOffPolicy.setCompany(new Company(getAuthUser().getCompanyId()));
    given(timeOffPolicyService.getTimeOffPolicyById(Mockito.any())).willReturn(timeOffPolicy);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/time-off-policies/1")
                    .headers(httpHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(timeOffPolicyWrapperDto)))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateTimeOffPolicyEmployeesInfo() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_POLICY.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final List<TimeOffPolicyUserFrontendDto> userStartBalances = new ArrayList<>();
    final TimeOffPolicyUserFrontendDto timeOffPolicyUserFrontendDto = new TimeOffPolicyUserFrontendDto();
    timeOffPolicyUserFrontendDto.setUserId("1");
    userStartBalances.add(timeOffPolicyUserFrontendDto);
    final TimeOffPolicyWrapperDto timeOffPolicyWrapperDto = new TimeOffPolicyWrapperDto();
    timeOffPolicyWrapperDto.setUserStartBalances(userStartBalances);

    final User targetUser = new User();
    targetUser.setCompany(new Company(getAuthUser().getCompanyId()));
    given(userService.findById("1")).willReturn(targetUser);
    final TimeOffPolicy timeOffPolicy = new TimeOffPolicy();
    timeOffPolicy.setCompany(new Company(getAuthUser().getCompanyId()));
    given(timeOffPolicyService.getTimeOffPolicyById(Mockito.any())).willReturn(timeOffPolicy);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/time-off-policies/employees/1")
                    .headers(httpHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(timeOffPolicyWrapperDto)))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetTimeOffBalances() throws Exception {
    setPermission(Name.MANAGE_USER_TIME_OFF_BALANCE.name());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    targetUser.setCompany(new Company(getAuthUser().getCompanyId()));
    given(userService.findById(getAuthUser().getId())).willReturn(targetUser);

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/time-off-policies/users/" + getAuthUser().getId() + "/balance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetAllPolicyUsersByUser() throws Exception {
    setPermission(Name.VIEW_SELF.name());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    targetUser.setCompany(new Company(getAuthUser().getCompanyId()));
    given(userService.findById(getAuthUser().getId())).willReturn(targetUser);

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/time-off-policies-users/users/" + getAuthUser().getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetTimeOffPolicyByTimeOffPolicyId() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_POLICY.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final TimeOffPolicy timeOffPolicy = new TimeOffPolicy();
    timeOffPolicy.setCompany(new Company(getAuthUser().getCompanyId()));
    given(timeOffPolicyService.getTimeOffPolicyById(Mockito.any())).willReturn(timeOffPolicy);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-off-policies/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetEmployeesByTimeOffPolicyId() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_POLICY.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final TimeOffPolicy timeOffPolicy = new TimeOffPolicy();
    timeOffPolicy.setCompany(new Company(getAuthUser().getCompanyId()));
    given(timeOffPolicyService.getTimeOffPolicyById(Mockito.any())).willReturn(timeOffPolicy);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-off-policies/1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testDeleteTimeOffPolicy() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_POLICY.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final TimeOffPolicy timeOffPolicy = new TimeOffPolicy();
    timeOffPolicy.setCompany(new Company(getAuthUser().getCompanyId()));
    given(timeOffPolicyService.getTimeOffPolicyById(Mockito.any())).willReturn(timeOffPolicy);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.delete("/company/time-off-policies/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testEnrollTimeOffPolicy() throws Exception {
    setPermission(Name.MANAGE_TIME_OFF_POLICY.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final TimeOffPolicy timeOffPolicy = new TimeOffPolicy();
    timeOffPolicy.setCompany(new Company(getAuthUser().getCompanyId()));
    given(timeOffPolicyService.getTimeOffPolicyById(Mockito.any())).willReturn(timeOffPolicy);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.delete("/company/time-off-policies/1/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetAllPolicies() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-off-policies")
                    .headers(httpHeaders)
                    .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetTimeOffBreakdown() throws Exception {
    setPermission(Name.MANAGE_SELF_TIME_OFF_BALANCE.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
    final User user = new User();
    user.setId(getAuthUser().getId());
    user.setCompany(new Company(getAuthUser().getCompanyId()));
    timeOffPolicyUser.setUser(user);
    given(timeOffPolicyUserService.findById(Mockito.any())).willReturn(timeOffPolicyUser);
    given(timeOffDetailService.getTimeOffBreakdown(Mockito.any(), Mockito.any()))
        .willReturn(new TimeOffBreakdownDto());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/time-off-balances/1/breakdown")
                    .headers(httpHeaders)
                    .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testAddTimeOffAdjustments() throws Exception {
    setPermission(Name.MANAGE_USER_TIME_OFF_BALANCE.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
    final User user = new User();
    user.setId(getAuthUser().getId());
    user.setCompany(new Company(getAuthUser().getCompanyId()));
    timeOffPolicyUser.setUser(user);
    given(timeOffPolicyUserService.findById(Mockito.any())).willReturn(timeOffPolicyUser);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/time-off-balances/1/adjustments")
                    .headers(httpHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(12)))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testCheckTimeOffAdjustments() throws Exception {
    setPermission(Name.MANAGE_USER_TIME_OFF_BALANCE.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final TimeOffPolicyUser timeOffPolicyUser = new TimeOffPolicyUser();
    final User user = new User();
    user.setId(getAuthUser().getId());
    user.setCompany(new Company(getAuthUser().getCompanyId()));
    timeOffPolicyUser.setUser(user);
    given(timeOffPolicyUserService.findById(Mockito.any())).willReturn(timeOffPolicyUser);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/time-off-policies-users/1/adjustments/check")
                    .headers(httpHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(12)))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testCheckHasTimeOffPolicies() throws Exception {
    setPermission(Name.VIEW_USER_TIME_OFF_BALANCE.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    targetUser.setCompany(new Company(getAuthUser().getCompanyId()));
    given(userService.findById(getAuthUser().getId())).willReturn(targetUser);

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(
                        "/company/time-off-policies/users/" + getAuthUser().getId() + "/has-policy")
                    .headers(httpHeaders)
                    .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
