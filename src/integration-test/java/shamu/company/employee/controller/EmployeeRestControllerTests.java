package shamu.company.employee.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shamu.company.WebControllerBaseTests;
import shamu.company.authorization.Permission.Name;
import shamu.company.common.exception.errormapping.EmailAlreadyVerifiedException;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Office;
import shamu.company.email.service.EmailService;
import shamu.company.employee.dto.EmailResendDto;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.dto.NewEmployeeJobInformationDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.service.EmployeeService;
import shamu.company.job.entity.Job;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.entity.User;
import shamu.company.utils.JsonUtil;
import shamu.company.utils.UuidUtil;

@WebMvcTest(controllers = EmployeeRestController.class)
class EmployeeRestControllerTests extends WebControllerBaseTests {

  @MockBean private EmployeeService employeeService;

  @MockBean private EmailService emailService;

  @Autowired private MockMvc mockMvc;

  @Test
  void testFindAllEmployees() throws Exception {

    final Page<JobUserListItem> result = new PageImpl<>(Collections.emptyList());
    given(userService.findAllEmployees(Mockito.any())).willReturn(result);

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/employees").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindMyTeam() throws Exception {
    final Page<JobUserListItem> result = new PageImpl<>(Collections.emptyList());
    given(userService.getMyTeam(Mockito.any(), Mockito.any())).willReturn(result);
    setPermission(Name.VIEW_MY_TEAM.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/employees/my-team").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindAllPolicyEmployees() throws Exception {
    given(userService.findAllJobUsers()).willReturn(Collections.emptyList());
    setPermission(Name.CREATE_USER.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/employees/jobs-users").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindWelcomeEmail() throws Exception {

    given(emailService.getWelcomeEmail(Mockito.any()))
        .willReturn(RandomStringUtils.randomAlphabetic(4));
    setPermission(Name.CREATE_USER.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/employees/welcome-email")
                    .headers(httpHeaders)
                    .content(RandomStringUtils.randomAlphabetic(4)))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getResponse().getContentAsString()).isNotEmpty();
  }

  @Nested
  class TestResendWelcomeEmail {

    private EmailResendDto emailResendDto;

    @BeforeEach
    void init() {
      targetUser.setId(UuidUtil.getUuidString());
      emailResendDto = new EmailResendDto();
      emailResendDto.setUserId(targetUser.getId());
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
      given(userService.findById(targetUser.getId())).willReturn(targetUser);
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.post("/company/employees/welcome-email/resend")
                  .content(JsonUtil.formatToString(emailResendDto))
                  .contentType(MediaType.APPLICATION_JSON)
                  .headers(httpHeaders))
          .andReturn();
    }
  }

  @Nested
  class TestAddEmployee {

    private EmployeeDto employeeDto;

    private Job job;

    private EmploymentType employmentType;

    private Office office;

    private User manager;

    @BeforeEach
    void init() {
      job = new Job();
      job.setId(UuidUtil.getUuidString());
      final Company company = new Company();
      company.setId(UuidUtil.getUuidString());

      employmentType = new EmploymentType();
      employmentType.setId(UuidUtil.getUuidString());

      manager = new User();
      manager.setId(UuidUtil.getUuidString());

      office = new Office();
      office.setId(UuidUtil.getUuidString());

      employeeDto = new EmployeeDto();
      final NewEmployeeJobInformationDto jobInfo = new NewEmployeeJobInformationDto();
      jobInfo.setJobId(job.getId());
      jobInfo.setEmploymentTypeId(employmentType.getId());
      jobInfo.setReportsTo(manager.getId());
      jobInfo.setOfficeId(office.getId());
      employeeDto.setJobInformation(jobInfo);

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
      given(jobService.findById(job.getId())).willReturn(job);
      given(companyService.findEmploymentTypeById(employmentType.getId()))
          .willReturn(employmentType);
      given(userService.findById(manager.getId())).willReturn(manager);
      given(companyService.findOfficeById(office.getId())).willReturn(office);
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.post("/company/employees")
                  .contentType(MediaType.APPLICATION_JSON)
                  .headers(httpHeaders)
                  .content(JsonUtil.formatToString(employeeDto)))
          .andReturn();
    }
  }

  @Test
  void testFindOrgChart() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/employees/org-chart")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetEmployeeInfoByUserId() throws Exception {
    setPermission(Name.VIEW_USER_PERSONAL.name());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    given(userService.findById(getAuthUser().getId())).willReturn(targetUser);

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/employees/" + getAuthUser().getId() + "/info")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Nested
  class SaveEmployeeSetUpInformation {

    private User currentUser;

    @BeforeEach
    void setUp() {
      currentUser = new User();
      currentUser.setId(getAuthUser().getId());
      given(userService.findById(getAuthUser().getId())).willReturn(currentUser);
    }

    @Test
    void whenEmployeeIsAlreadyVerified_thenThrowForbidden() throws Exception {

      currentUser.setVerifiedAt(new Timestamp(new Date().getTime()));
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
      final EmployeeDto postBody = new EmployeeDto();
      postBody.setEmailWork(getAuthUser().getEmail());
      final MvcResult response =
          mockMvc
              .perform(
                  MockMvcRequestBuilders.patch("/company/employees/current")
                      .contentType(MediaType.APPLICATION_JSON)
                      .headers(httpHeaders)
                      .content(JsonUtil.formatToString(postBody)))
              .andReturn();

      assertThat(response.getResolvedException()).isInstanceOf(EmailAlreadyVerifiedException.class);
    }

    @Test
    void whenNotVerified_thenShouldSucceed() throws Exception {
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
      final EmployeeDto postBody = new EmployeeDto();
      postBody.setEmailWork(getAuthUser().getEmail());
      final MvcResult response =
          mockMvc
              .perform(
                  MockMvcRequestBuilders.patch("/company/employees/current")
                      .contentType(MediaType.APPLICATION_JSON)
                      .headers(httpHeaders)
                      .content(JsonUtil.formatToString(postBody)))
              .andReturn();

      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }
  }
}
