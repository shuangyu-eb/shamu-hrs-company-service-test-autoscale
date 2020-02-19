package shamu.company.benefit.controller;

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
import shamu.company.benefit.dto.BenefitDependentCreateDto;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.entity.DependentRelationship;
import shamu.company.benefit.entity.mapper.BenefitPlanDependentMapper;
import shamu.company.benefit.repository.BenefitPlanDependentRelationshipRepository;
import shamu.company.company.entity.Company;
import shamu.company.crypto.EncryptorUtil;
import shamu.company.server.dto.AuthUser;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.entity.User;
import shamu.company.utils.JsonUtil;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebMvcTest(controllers = BenefitPlanDependentController.class)
class BenefitPlanDependentControllerTests extends WebControllerBaseTests {

  @MockBean
  BenefitPlanDependentMapper benefitPlanDependentMapper;

  @MockBean
  BenefitPlanDependentRelationshipRepository relationshipRepository;

  @MockBean
  EncryptorUtil encryptorUtil;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testCreateBenefitDependents() throws Exception {
    setPermission(Permission.Name.EDIT_USER.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());
    final BenefitDependentCreateDto benefitDependentCreateDto = new BenefitDependentCreateDto();
    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .post("/company/employee/" + getAuthUser().getId() + "/user-dependent-contacts")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(benefitDependentCreateDto))).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetDependentContacts() throws Exception {
    setPermission(Permission.Name.EDIT_USER.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());
    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    List<BenefitPlanDependent> benefitPlanDependents = new ArrayList<>();
    given(benefitPlanDependentService.getDependentListsByEmployeeId(getAuthUser().getId()))
        .willReturn(benefitPlanDependents);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/users/" + getAuthUser().getId() + "/user-dependent-contacts")
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    Mockito.verify(benefitPlanDependentMapper, Mockito.times(0))
        .convertToBenefitDependentDto(Mockito.any());
  }

  @Test
  void testUpdateDependentContact() throws Exception {
    setPermission(Permission.Name.EDIT_USER.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());
    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    BenefitDependentCreateDto benefitDependentCreateDto = new BenefitDependentCreateDto();
    benefitDependentCreateDto.setId("id");
    final BenefitPlanDependent dependent = new BenefitPlanDependent();
    given(benefitPlanDependentService.findDependentById(benefitDependentCreateDto.getId()))
        .willReturn(dependent);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .patch("/company/users/" + getAuthUser().getId() + "/user-dependent-contacts")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(benefitDependentCreateDto))).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testDeleteEmergencyContacts() throws Exception {
    setPermission(Permission.Name.EDIT_USER.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());
    BenefitPlanDependent benefitPlanDependent = new BenefitPlanDependent();
    benefitPlanDependent.setEmployee(targetUser);
    given(benefitPlanDependentService.findDependentById("1")).willReturn(benefitPlanDependent);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .delete("/company/user-dependent-contacts/1/dependent")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetAllDependentRelationships() throws Exception {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    List<DependentRelationship> results = new ArrayList<>();
    given(relationshipRepository.findAll()).willReturn(results);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/dependent-relationships")
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetUserDependentContacts() throws Exception {
    setPermission(Permission.Name.EDIT_SELF.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());
    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/users/" + getAuthUser().getId() + "/benefit-plan-dependents")
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
