package shamu.company.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shamu.company.WebControllerBaseTests;
import shamu.company.company.entity.Company;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.server.CompanyServerController;
import shamu.company.server.dto.DocumentRequestEmailDto;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.UserRole;
import shamu.company.utils.JsonUtil;

@WebMvcTest(controllers = CompanyServerController.class)
public class CompanyServerControllerTests extends WebControllerBaseTests {
  @Autowired private MockMvc mockMvc;

  @Test
  void testFindCurrentUser() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/server/company/users/current").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindUsersById() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    given(companyUserService.findAllById(Mockito.anyList())).willReturn(new ArrayList<>());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/server/company/users/id?ids=[1]").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindUserById() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    final User user = new User();
    final UserPersonalInformation information = new UserPersonalInformation();
    final UserContactInformation userContactInformation = new UserContactInformation();
    userContactInformation.setEmailWork("1");
    information.setFirstName("x");
    information.setLastName("y");
    user.setId("1");
    user.setUserPersonalInformation(information);
    user.setUserContactInformation(userContactInformation);
    user.setImageUrl("x");
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    given(companyUserService.findUserById(Mockito.anyString())).willReturn(user);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/server/company/user/id?id=1").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindAllUsers() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    given(companyUserService.findAllUsers(Mockito.anyString())).willReturn(new ArrayList<>());
    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/server/company/users").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testSendDocumentRequestEmail() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    final DocumentRequestEmailDto documentRequestEmailDto = new DocumentRequestEmailDto();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/server/company/emails")
                    .headers(httpHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(documentRequestEmailDto)))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindAllEmployeesByName() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    final EmployeeListSearchCondition employeeListSearchCondition =
        new EmployeeListSearchCondition();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    given(companyUserService.findAllEmployees(Mockito.any(), Mockito.any()))
        .willReturn(new PageImpl<JobUserListItem>(new ArrayList<>()));
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/server/company/employees")
                    .headers(httpHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(employeeListSearchCondition)))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetUserById() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final User user = new User();
    final Company company = new Company();
    final UserRole role = new UserRole();
    role.setName("ADMIN");
    company.setId("1");
    final UserContactInformation userContactInformation = new UserContactInformation();
    userContactInformation.setEmailWork("1");
    user.setId("1");
    user.setUserContactInformation(userContactInformation);
    user.setImageUrl("x");
    user.setUserRole(role);
    user.setCompany(company);
    given(companyUserService.findUserByUserId(Mockito.anyString())).willReturn(user);
    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/server/company/user/1").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
