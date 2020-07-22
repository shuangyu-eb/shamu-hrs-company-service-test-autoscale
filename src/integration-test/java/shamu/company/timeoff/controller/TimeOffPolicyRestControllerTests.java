package shamu.company.timeoff.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
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
import shamu.company.utils.UuidUtil;

@WebMvcTest(controllers = TimeOffPolicyRestController.class)
class TimeOffPolicyRestControllerTests extends WebControllerBaseTests {

  @MockBean private TimeOffDetailService timeOffDetailService;

  @Autowired private MockMvc mockMvc;

  @Nested
  class TestCreateTimeOffPolicy {

    private TimeOffPolicyWrapperDto timeOffPolicyWrapperDto;

    @BeforeEach
    void init() {
      timeOffPolicyWrapperDto = new TimeOffPolicyWrapperDto();
    }

    private class CommonTests {

      @Test
      void asManager_thenShouldFailed() throws Exception {
        buildAuthUserAsManager();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }

      @Test
      void asEmployee_thenShouldFailed() throws Exception {
        buildAuthUserAsEmployee();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }

      @Test
      void asDeactivatedUser_thenShouldFailed() throws Exception {
        buildAuthUserAsDeactivatedUser();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }
    }

    @Nested
    class SameCompany extends CommonTests {

      @BeforeEach
      void init() {
        targetUser.setCompany(company);
        targetUser.setId(UuidUtil.getUuidString());
        final TimeOffPolicyUserFrontendDto timeOffPolicyUserFrontendDto =
            new TimeOffPolicyUserFrontendDto();
        timeOffPolicyUserFrontendDto.setUserId(targetUser.getId());
        timeOffPolicyWrapperDto.setUserStartBalances(
            Collections.singletonList(timeOffPolicyUserFrontendDto));
        setGiven();
      }

      @Test
      void asAdmin_thenShouldSuccess() throws Exception {
        buildAuthUserAsAdmin();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
      }
    }

    @Nested
    class DifferentCompany extends CommonTests {

      @BeforeEach
      void init() {
        targetUser.setCompany(theOtherCompany);
        targetUser.setId(UuidUtil.getUuidString());
        final TimeOffPolicyUserFrontendDto timeOffPolicyUserFrontendDto =
            new TimeOffPolicyUserFrontendDto();
        timeOffPolicyUserFrontendDto.setUserId(targetUser.getId());
        timeOffPolicyWrapperDto.setUserStartBalances(
            Collections.singletonList(timeOffPolicyUserFrontendDto));
        setGiven();
      }

      @Test
      void asAdmin_thenShouldFailed() throws Exception {
        buildAuthUserAsAdmin();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }
    }

    @Nested
    class IncludeSameCompanyAndDifferentCompany extends DifferentCompany {

      @Override
      @BeforeEach
      void init() {
        targetUser.setCompany(company);
        targetUser.setId(UuidUtil.getUuidString());
        final TimeOffPolicyUserFrontendDto sameCompanyDto = new TimeOffPolicyUserFrontendDto();
        sameCompanyDto.setUserId(targetUser.getId());

        final User differentCompanyUser = new User(UuidUtil.getUuidString());
        differentCompanyUser.setCompany(theOtherCompany);
        final TimeOffPolicyUserFrontendDto differentCompanyDto = new TimeOffPolicyUserFrontendDto();
        sameCompanyDto.setUserId(differentCompanyUser.getId());

        final List<TimeOffPolicyUserFrontendDto> dtos = new ArrayList<>();
        dtos.add(sameCompanyDto);
        dtos.add(differentCompanyDto);
        timeOffPolicyWrapperDto.setUserStartBalances(dtos);

        setGiven();
        given(userService.findById(differentCompanyUser.getId())).willReturn(differentCompanyUser);
      }
    }

    private void setGiven() {
      given(userService.findById(targetUser.getId())).willReturn(targetUser);
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.post("/company/time-off-policies")
                  .headers(httpHeaders)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(JsonUtil.formatToString(timeOffPolicyWrapperDto)))
          .andReturn();
    }
  }

  @Nested
  @Data
  class TestUpdateTimeOffPolicy {

    private TimeOffPolicyWrapperDto timeOffPolicyWrapperDto;

    private TimeOffPolicy timeOffPolicy;

    @BeforeEach
    void init() {
      timeOffPolicyWrapperDto = new TimeOffPolicyWrapperDto();
      timeOffPolicy = new TimeOffPolicy();
      timeOffPolicy.setId(UuidUtil.getUuidString());
    }

    private class CommonTests {

      @Test
      void asManager_thenShouldFailed() throws Exception {
        buildAuthUserAsManager();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }

      @Test
      void asEmployee_thenShouldFailed() throws Exception {
        buildAuthUserAsEmployee();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }

      @Test
      void asDeactivatedUser_thenShouldFailed() throws Exception {
        buildAuthUserAsDeactivatedUser();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }
    }

    @Nested
    class SameCompany extends CommonTests {

      @BeforeEach
      void init() {
        targetUser.setId(UuidUtil.getUuidString());
        targetUser.setCompany(company);

        final TimeOffPolicyUserFrontendDto timeOffPolicyUserFrontendDto =
            new TimeOffPolicyUserFrontendDto();
        timeOffPolicyUserFrontendDto.setUserId(targetUser.getId());
        timeOffPolicyWrapperDto.setUserStartBalances(
            Collections.singletonList(timeOffPolicyUserFrontendDto));

        setGiven();
      }

      @Test
      void asAdmin_thenShouldSuccess() throws Exception {
        buildAuthUserAsAdmin();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
      }
    }

    @Nested
    class DifferentCompany extends CommonTests {

      @BeforeEach
      void init() {
        targetUser.setId(UuidUtil.getUuidString());
        targetUser.setCompany(theOtherCompany);

        final TimeOffPolicyUserFrontendDto timeOffPolicyUserFrontendDto =
            new TimeOffPolicyUserFrontendDto();
        timeOffPolicyUserFrontendDto.setUserId(targetUser.getId());
        timeOffPolicyWrapperDto.setUserStartBalances(
            Collections.singletonList(timeOffPolicyUserFrontendDto));

        setGiven();
      }

      @Test
      void asAdmin_thenShouldFailed() throws Exception {
        buildAuthUserAsAdmin();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }
    }

    @Nested
    class IncludeSameCompanyAndDifferentCompany extends DifferentCompany {

      @Override
      @BeforeEach
      void init() {
        targetUser.setCompany(company);
        targetUser.setId(UuidUtil.getUuidString());
        final TimeOffPolicyUserFrontendDto sameCompanyDto = new TimeOffPolicyUserFrontendDto();
        sameCompanyDto.setUserId(targetUser.getId());

        final User differentCompanyUser = new User(UuidUtil.getUuidString());
        differentCompanyUser.setCompany(theOtherCompany);
        final TimeOffPolicyUserFrontendDto differentCompanyDto = new TimeOffPolicyUserFrontendDto();
        sameCompanyDto.setUserId(differentCompanyUser.getId());

        final List<TimeOffPolicyUserFrontendDto> dtos = new ArrayList<>();
        dtos.add(sameCompanyDto);
        dtos.add(differentCompanyDto);
        timeOffPolicyWrapperDto.setUserStartBalances(dtos);

        setGiven();
        given(userService.findById(differentCompanyUser.getId())).willReturn(differentCompanyUser);
      }
    }

    void setGiven() {
      given(userService.findById(targetUser.getId())).willReturn(targetUser);
      given(timeOffPolicyService.getTimeOffPolicyById(timeOffPolicy.getId()))
          .willReturn(timeOffPolicy);
    }

    MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.patch("/company/time-off-policies/" + timeOffPolicy.getId())
                  .headers(httpHeaders)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(JsonUtil.formatToString(timeOffPolicyWrapperDto)))
          .andReturn();
    }
  }

  @Nested
  class TestUpdateTimeOffPolicyEmployeesInfo extends TestUpdateTimeOffPolicy {

    @Override
    MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.patch(
                      "/company/time-off-policies/employees/" + getTimeOffPolicy().getId())
                  .headers(httpHeaders)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(JsonUtil.formatToString(getTimeOffPolicyWrapperDto())))
          .andReturn();
    }
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

  @Nested
  class TestDeleteTimeOffPolicy {

    private TimeOffPolicy timeOffPolicy;

    @BeforeEach
    void init() {
      timeOffPolicy = new TimeOffPolicy();
      timeOffPolicy.setId(UuidUtil.getUuidString());
      setGiven();
    }

    @Test
    void asAdmin_thenShouldSuccess() throws Exception {
      buildAuthUserAsAdmin();
      final MvcResult response = getResponse();
      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void asManager_thenShouldFailed() throws Exception {
      buildAuthUserAsManager();
      final MvcResult response = getResponse();
      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void asEmployee_thenShouldFailed() throws Exception {
      buildAuthUserAsEmployee();
      final MvcResult response = getResponse();
      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void asDeactivatedUser_thenShouldFailed() throws Exception {
      buildAuthUserAsDeactivatedUser();
      final MvcResult response = getResponse();
      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    private void setGiven() {
      given(timeOffPolicyService.getTimeOffPolicyById(timeOffPolicy.getId()))
          .willReturn(timeOffPolicy);
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.delete("/company/time-off-policies/" + timeOffPolicy.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .headers(httpHeaders))
          .andReturn();
    }
  }

  @Nested
  class TestEnrollTimeOffPolicy {

    private TimeOffPolicy timeOffPolicy;

    private TimeOffPolicy rolledPolicy;

    @BeforeEach
    void init() {
      timeOffPolicy = new TimeOffPolicy();
      timeOffPolicy.setId(UuidUtil.getUuidString());
      rolledPolicy = new TimeOffPolicy();
      rolledPolicy.setId(UuidUtil.getUuidString());
      setGiven();
    }

    @Test
    void asAdmin_thenShouldSuccess() throws Exception {
      buildAuthUserAsAdmin();
      final MvcResult response = getResponse();
      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void asManager_thenShouldFailed() throws Exception {
      buildAuthUserAsManager();
      final MvcResult response = getResponse();
      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void asEmployee_thenShouldFailed() throws Exception {
      buildAuthUserAsEmployee();
      final MvcResult response = getResponse();
      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void asDeactivatedUser_thenShouldFailed() throws Exception {
      buildAuthUserAsDeactivatedUser();
      final MvcResult response = getResponse();
      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    private void setGiven() {
      given(timeOffPolicyService.getTimeOffPolicyById(timeOffPolicy.getId()))
          .willReturn(timeOffPolicy);
      given(timeOffPolicyService.getTimeOffPolicyById(rolledPolicy.getId()))
          .willReturn(rolledPolicy);
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.delete(
                      String.format(
                          "/company/time-off-policies/%s/%s",
                          timeOffPolicy.getId(), rolledPolicy.getId()))
                  .contentType(MediaType.APPLICATION_JSON)
                  .headers(httpHeaders))
          .andReturn();
    }
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

  @Nested
  class TestAddTimeOffAdjustments {

    private TimeOffPolicyUser timeOffPolicyUser;

    @BeforeEach
    void init() {
      timeOffPolicyUser = new TimeOffPolicyUser();
      timeOffPolicyUser.setId(UuidUtil.getUuidString());
      timeOffPolicyUser.setUser(targetUser);
      setGiven();
    }

    private class CommonTests {

      @Test
      void asManager_thenShouldFailed() throws Exception {
        buildAuthUserAsManager();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }

      @Test
      void asEmployee_thenShouldFailed() throws Exception {
        buildAuthUserAsEmployee();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }

      @Test
      void asDeactivatedUser_thenShouldFailed() throws Exception {
        buildAuthUserAsDeactivatedUser();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }
    }

    @Nested
    class SameUser extends CommonTests {

      @BeforeEach
      void init() {
        targetUser.setId(UuidUtil.getUuidString());
        targetUser.setCompany(company);
      }

      @Test
      void asAdmin_thenShouldSuccess() throws Exception {
        buildAuthUserAsAdmin();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
      }
    }

    @Nested
    class DifferentCompany extends CommonTests {

      @BeforeEach
      void init() {
        targetUser.setId(UuidUtil.getUuidString());
        targetUser.setCompany(theOtherCompany);
      }

      @Test
      void asAdmin_thenShouldFailed() throws Exception {
        buildAuthUserAsAdmin();
        final MvcResult response = getResponse();
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
      }
    }

    private void setGiven() {
      given(timeOffPolicyUserService.findById(timeOffPolicyUser.getId()))
          .willReturn(timeOffPolicyUser);
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.post(
                      String.format(
                          "/company/time-off-balances/%s/adjustments", timeOffPolicyUser.getId()))
                  .headers(httpHeaders)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(JsonUtil.formatToString(12)))
          .andReturn();
    }
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
