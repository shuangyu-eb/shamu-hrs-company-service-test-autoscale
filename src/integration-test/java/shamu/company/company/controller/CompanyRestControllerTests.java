package shamu.company.company.controller;

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
import shamu.company.authorization.Permission;
import shamu.company.company.dto.CompanyBenefitsSettingDto;
import shamu.company.company.dto.OfficeCreateDto;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.mapper.OfficeMapper;
import shamu.company.employee.service.EmployeeService;
import shamu.company.job.entity.Job;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.utils.JsonUtil;

@WebMvcTest(controllers = CompanyRestController.class)
public class CompanyRestControllerTests extends WebControllerBaseTests {

  @MockBean private EmployeeService employeeService;
  @MockBean private OfficeMapper officeMapper;

  @Autowired private MockMvc mockMvc;

  @Test
  void testFindDepartments() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/departments").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testCreateDepartment() throws Exception {
    setPermission(Permission.Name.CREATE_DEPARTMENT.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/departments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders)
                    .content("name"))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testSaveJobsByDepartment() throws Exception {
    setPermission(Permission.Name.CREATE_JOB.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    final Job job = new Job();
    final Department department = new Department();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    job.setId("1");
    job.setTitle("title");
    given(companyService.findDepartmentsById("1")).willReturn(department);
    given(companyService.saveJob(Mockito.anyString())).willReturn(job);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/jobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders)
                    .content("name"))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindOffices() throws Exception {
    setPermission(Permission.Name.VIEW_USER_JOB.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/offices").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testSaveOffice() throws Exception {
    final OfficeCreateDto officeCreateDto = new OfficeCreateDto();
    officeCreateDto.setStateId("1");
    officeCreateDto.setCity("city");
    officeCreateDto.setOfficeName("officeName");
    officeCreateDto.setStreet1("street1");
    officeCreateDto.setPostalCode("postalCode");

    setPermission(Permission.Name.CREATE_OFFICE.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/company/offices")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(httpHeaders)
                    .content(JsonUtil.formatToString(officeCreateDto)))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindUsers() throws Exception {
    final List<User> list = new ArrayList<>();
    final User user = new User();
    list.add(user);
    setPermission(Permission.Name.VIEW_USER_JOB.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    given(employeeService.findAllActiveUsers()).willReturn(list);
    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/user-options").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindUsersFromDepartment() throws Exception {
    setPermission(Permission.Name.VIEW_JOB.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    final User user = new User();
    final UserPersonalInformation userInfo = new UserPersonalInformation();
    userInfo.setFirstName("name");
    userInfo.setLastName("last");
    userInfo.setId("1");
    user.setUserPersonalInformation(userInfo);
    user.setUserStatus(new UserStatus(Status.ACTIVE.name()));
    final List<User> list = new ArrayList<>();
    list.add(user);

    final Department department = new Department();
    given(companyService.findDepartmentsById("1")).willReturn(department);
    given(userService.findById("1")).willReturn(user);
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    given(employeeService.findAllActiveUsers()).willReturn(list);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/manager-candidate/1/users")
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindCompanyBenefitsSetting() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/benefits-setting").headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateBenefitSettingAutomaticRollover() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/benefits-setting/automatic-rollover")
                    .headers(httpHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("true"))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateEnrollmentPeriod() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    final CompanyBenefitsSettingDto companyBenefitsSettingDto = new CompanyBenefitsSettingDto();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/benefits-setting/enrollment-period")
                    .headers(httpHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(companyBenefitsSettingDto)))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testFindIsPaidHolidayAutoEnrolled() throws Exception {
    setPermission(Permission.Name.EDIT_PAID_HOLIDAY.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final Company company = new Company();
    company.setIsPaidHolidaysAutoEnroll(true);
    given(companyService.getCompany()).willReturn(company);
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/paid-holidays-auto-enrolled")
                    .headers(httpHeaders))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateIsPaidHolidayAutoEnrolled() throws Exception {
    setPermission(Permission.Name.EDIT_PAID_HOLIDAY.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/company/global-setting/paid-holidays-auto-enrolled")
                    .headers(httpHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JsonUtil.formatToString(true)))
            .andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
