package shamu.company.job.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import shamu.company.authorization.Permission;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.employee.dto.SelectFieldSizeDto;
import shamu.company.job.JobController;
import shamu.company.job.dto.JobSelectOptionUpdateDto;
import shamu.company.job.dto.JobSelectOptionUpdateField;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.server.dto.AuthUser;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.entity.User;
import shamu.company.utils.JsonUtil;
import shamu.company.utils.UuidUtil;

@WebMvcTest(controllers = JobController.class)
public class JobControllerTests extends WebControllerBaseTests {
  @Autowired private MockMvc mockMvc;

  @Test
  void testFindJobMessage() throws Exception {
    setPermission(Permission.Name.VIEW_USER_JOB.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final BasicJobInformationDto basicJobInformationDto = new BasicJobInformationDto();
    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    targetUser.setId(currentUser.getId());
    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    given(jobUserService.findJobMessage(currentUser.getId(), currentUser.getId()))
        .willReturn(basicJobInformationDto);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/users/" + currentUser.getId() + "/job")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Nested
  class TestUpdateJobInfo {

    private final JobUpdateDto jobUpdateDto = new JobUpdateDto();

    @Nested
    class SameUser {

      @BeforeEach
      void init() {
        targetUser.setId(currentUser.getId());
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
    }

    @Nested
    class OtherUser {

      @BeforeEach
      void init() {
        targetUser.setId(UuidUtil.getUuidString());
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
        final User manager = new User(currentUser.getId());
        targetUser.setManagerUser(manager);
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

    private void setGiven() {
      given(userService.findById(currentUser.getId())).willReturn(targetUser);
    }

    private MvcResult getResponse() throws Exception {
      return mockMvc
          .perform(
              MockMvcRequestBuilders.patch("/company/users/" + currentUser.getId() + "/jobs")
                  .contentType(MediaType.APPLICATION_JSON)
                  .headers(httpHeaders)
                  .content(JsonUtil.formatToString(jobUpdateDto)))
          .andReturn();
    }
  }

  @Test
  void testUpdateJobSelectOption() throws Exception {
    setPermission(Permission.Name.EDIT_USER.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final JobSelectOptionUpdateDto jobSelectOptionUpdateDto = new JobSelectOptionUpdateDto();
    jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.JOB_TITLE);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/jobs/select/option/update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders)
                    .content(JsonUtil.formatToString(jobSelectOptionUpdateDto)))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testDeleteJobSelectOption() throws Exception {
    setPermission(Permission.Name.EDIT_USER.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final JobSelectOptionUpdateDto jobSelectOptionUpdateDto = new JobSelectOptionUpdateDto();
    jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.JOB_TITLE);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.delete("/company/jobs/select/option/delete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders)
                    .content(JsonUtil.formatToString(jobSelectOptionUpdateDto)))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindJobsByDepartment() throws Exception {
    setPermission(Permission.Name.VIEW_JOB.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final List<SelectFieldSizeDto> selectFieldSizeDtos = Collections.emptyList();
    final String departmentId = "1";
    final Department department = new Department();
    department.setId(departmentId);
    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setId(currentUser.getId());
    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    given(companyService.findDepartmentsById(departmentId)).willReturn(department);
    given(jobUserService.findJobs()).willReturn(selectFieldSizeDtos);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/jobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindHomeAndOfficeAddresses() throws Exception {
    setPermission(Permission.Name.VIEW_USER_JOB.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/job/homeAndOfficeAddresses")
                    .content(JsonUtil.formatToString(new ArrayList<>()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
