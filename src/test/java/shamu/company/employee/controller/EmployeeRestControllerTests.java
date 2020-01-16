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
import shamu.company.common.exception.ForbiddenException;
import shamu.company.company.entity.Company;
import shamu.company.email.EmailService;
import shamu.company.employee.dto.EmailResendDto;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.service.EmployeeService;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.entity.User;
import shamu.company.utils.JsonUtil;
import shamu.company.utils.UuidUtil;

@WebMvcTest(controllers = EmployeeRestController.class)
class EmployeeRestControllerTests extends WebControllerBaseTests {

  @MockBean
  private EmployeeService employeeService;

  @MockBean
  private EmailService emailService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testFindAllEmployees() throws Exception {

    final Page<JobUserListItem> result = new PageImpl<>(Collections.emptyList());
    given(userService.findAllEmployees(Mockito.any(), Mockito.any())).willReturn(result);

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/employees")
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindMyTeam() throws Exception {
    final Page<JobUserListItem> result = new PageImpl<>(Collections.emptyList());
    given(userService.getMyTeam(Mockito.any(), Mockito.any())).willReturn(result);
    setPermission(Name.VIEW_MY_TEAM.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/employees/my-team")
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindAllPolicyEmployees() throws Exception {
    given(userService.findAllJobUsers(Mockito.any())).willReturn(Collections.emptyList());
    setPermission(Name.CREATE_USER.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/employees/jobs-users")
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindWelcomeEmail() throws Exception {

    given(emailService.getWelcomeEmail(Mockito.any())).willReturn(RandomStringUtils.randomAlphabetic(4));
    setPermission(Name.CREATE_USER.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .post("/company/employees/welcome-email")
        .headers(httpHeaders)
        .content(RandomStringUtils.randomAlphabetic(4))).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getResponse().getContentAsString()).isNotEmpty();
  }

  @Test
  void testResendWelcomeEmail() throws Exception {

    setPermission(Name.EDIT_USER.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final EmailResendDto emailResendDto = new EmailResendDto();
    emailResendDto.setUserId(UuidUtil.getUuidString());
    emailResendDto.setEmail("example@example.com");

    final User targetUser = new User();
    targetUser.setCompany(new Company(getAuthUser().getCompanyId()));
    given(userService.findById(emailResendDto.getUserId())).willReturn(targetUser);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .post("/company/employees/welcome-email/resend")
        .content(JsonUtil.formatToString(emailResendDto))
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testAddEmployee() throws Exception {
    setPermission(Name.CREATE_USER.name());

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .post("/company/employees")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(new EmployeeDto()))).andReturn();

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
      final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
          .patch("/company/employees/current")
          .contentType(MediaType.APPLICATION_JSON)
          .headers(httpHeaders)
          .content(JsonUtil.formatToString(postBody))).andReturn();

      assertThat(response.getResolvedException()).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void whenNotVerified_thenShouldSucceed() throws Exception {
      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
      final EmployeeDto postBody = new EmployeeDto();
      postBody.setEmailWork(getAuthUser().getEmail());
      final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
          .patch("/company/employees/current")
          .contentType(MediaType.APPLICATION_JSON)
          .headers(httpHeaders)
          .content(JsonUtil.formatToString(postBody))).andReturn();

      assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }
  }

  @Test
  void testFindOrgChart() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/employees/org-chart")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetEmployeeInfoByUserId() throws Exception {
    setPermission(Name.VIEW_USER_PERSONAL.name());

    final User targetUser = new User();
    targetUser.setId(getAuthUser().getId());
    targetUser.setCompany(new Company(getAuthUser().getCompanyId()));
    given(userService.findById(getAuthUser().getId())).willReturn(targetUser);

    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/employees/" + getAuthUser().getId() +"/info")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
