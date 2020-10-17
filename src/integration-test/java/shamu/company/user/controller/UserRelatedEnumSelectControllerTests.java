package shamu.company.user.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import shamu.company.WebControllerBaseTests;
import shamu.company.employee.service.EmploymentTypeService;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.service.CompensationFrequencyService;
import shamu.company.user.service.DeactivationReasonService;
import shamu.company.user.service.EmployeeTypesService;
import shamu.company.user.service.EthnicityService;
import shamu.company.user.service.GenderService;
import shamu.company.user.service.MaritalStatusService;
import shamu.company.user.service.RetirementPayTypeService;
import shamu.company.user.service.RetirementTypeService;
import shamu.company.user.service.UserRoleService;
import shamu.company.user.service.UserStatusService;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = UserRelatedEnumSelectController.class)
public class UserRelatedEnumSelectControllerTests extends WebControllerBaseTests {

  @MockBean private CompensationFrequencyService compensationFrequencyService;

  @MockBean private DeactivationReasonService deactivationReasonService;

  @MockBean private EthnicityService ethnicityService;

  @MockBean private GenderService genderService;

  @MockBean private MaritalStatusService maritalStatusService;

  @MockBean private RetirementTypeService retirementTypeService;

  @MockBean private UserRoleService userRoleService;

  @MockBean private UserStatusService userStatusService;

  @MockBean private EmployeeTypesService employeeTypesService;

  @MockBean private EmploymentTypeService employmentTypeService;

  @MockBean private RetirementPayTypeService retirementPayTypeService;
  @Autowired private MockMvc mockMvc;

  @Test
  void testGetCompensationFrequencies() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/compensation-frequencies")
                    .headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetDeactivationReasons() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/company/deactivation-reasons").headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetEthnicities() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/ethnicities").headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetGenders() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/genders").headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetMaritalStatuses() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/marital-statuses").headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetRetirementTypes() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/retirement-types").headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetAllUserRoles() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/user-roles").headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetAllUserStatuses() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/user-statuses").headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetAllEmployeeTypes() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/employee-types").headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetAllEmploymentTypes() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/employment-types").headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetRetirementPayTypes() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());

    final MvcResult response =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/company/retirement-pay-types").headers(httpHeaders))
            .andReturn();

    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
