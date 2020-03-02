package shamu.company.job.controller;

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

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;


@WebMvcTest(controllers = JobController.class)
public class JobControllerTests extends WebControllerBaseTests {
  @Autowired
  private MockMvc mockMvc;

  @Test
  void testFindJobMessage() throws Exception{
    setPermission(Permission.Name.VIEW_USER_JOB.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final BasicJobInformationDto basicJobInformationDto = new BasicJobInformationDto();
    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());
    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    given(jobUserService.findJobMessage(currentUser.getId(), currentUser.getId())).willReturn(basicJobInformationDto);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/users/"+currentUser.getId()+"/job")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateJobInfo() throws Exception {
    setPermission(Permission.Name.EDIT_USER.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());
    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    JobUpdateDto jobUpdateDto = new JobUpdateDto();
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .patch("/company/users/"+currentUser.getId()+"/jobs")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(jobUpdateDto))).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateJobSelectOption() throws Exception {
    setPermission(Permission.Name.EDIT_USER.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    JobSelectOptionUpdateDto jobSelectOptionUpdateDto = new JobSelectOptionUpdateDto();
    jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.EMPLOYMENT_TYPE);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .patch("/company/jobs/select/option/update")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(jobSelectOptionUpdateDto))).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testDeleteJobSelectOption() throws Exception {
    setPermission(Permission.Name.EDIT_USER.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    JobSelectOptionUpdateDto jobSelectOptionUpdateDto = new JobSelectOptionUpdateDto();
    jobSelectOptionUpdateDto.setUpdateField(JobSelectOptionUpdateField.EMPLOYMENT_TYPE);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .delete("/company/jobs/select/option/delete")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(jobSelectOptionUpdateDto))).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindJobsByDepartment() throws Exception {
    setPermission(Permission.Name.VIEW_JOB.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    List<SelectFieldSizeDto> selectFieldSizeDtos = Collections.emptyList();
    String departmentId = "1";
    Department department = new Department();
    department.setId(departmentId);
    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());
    department.setCompany(company);
    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    given(companyService.findDepartmentsById(departmentId)).willReturn(department);
    given(jobUserService.findJobsByDepartmentId(departmentId)).willReturn(selectFieldSizeDtos);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/departments/"+departmentId+"/jobs")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
