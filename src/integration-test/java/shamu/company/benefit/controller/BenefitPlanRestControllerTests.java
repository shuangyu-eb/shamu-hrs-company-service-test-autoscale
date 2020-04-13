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
import shamu.company.benefit.dto.*;
import shamu.company.benefit.entity.BenefitCoverages;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.benefit.entity.mapper.BenefitPlanMapper;
import shamu.company.benefit.entity.mapper.BenefitPlanTypeMapper;
import shamu.company.company.entity.Company;
import shamu.company.server.dto.AuthUser;
import shamu.company.tests.utils.JwtUtil;
import shamu.company.user.entity.User;
import shamu.company.utils.JsonUtil;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.BDDMockito.given;

@WebMvcTest(controllers = BenefitPlanRestController.class)
public class BenefitPlanRestControllerTests extends WebControllerBaseTests {

  @MockBean
  BenefitPlanTypeMapper benefitPlanTypeMapper;

  @MockBean
  BenefitPlanMapper benefitPlanMapper;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testsFindAllBenefitPlanTypes() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT_PLAN.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final List<BenefitPlanType> benefitPlanTypes = new ArrayList<>();
    given(benefitPlanTypeService.findAllBenefitPlanTypes()).willReturn(benefitPlanTypes);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/benefit-plan-types/default")
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    Mockito.verify(benefitPlanTypeMapper, Mockito.times(1))
        .convertAllToDefaultBenefitPlanTypeDtos(Mockito.any());
  }

  @Test
  void testCreateBenefitPlan() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT_PLAN.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final NewBenefitPlanWrapperDto data = new NewBenefitPlanWrapperDto();
    final BenefitPlanCreateDto benefitPlanCreateDto = new BenefitPlanCreateDto();
    data.setBenefitPlan(benefitPlanCreateDto);

    final List<BenefitPlanCoverageDto> coverageDtos = new ArrayList<>();
    data.setCoverages(coverageDtos);
    final BenefitPlanCoverageDto benefitPlanCoverageDto = new BenefitPlanCoverageDto();
    benefitPlanCoverageDto.setId("1");
    coverageDtos.add(benefitPlanCoverageDto);
    final List<BenefitCoverages> coverageEns = new ArrayList<>();
    final BenefitCoverages benefitCoverages = new BenefitCoverages();
    benefitCoverages.setId("1");
    coverageEns.add(benefitCoverages);

    given(benefitPlanService.findPlansWhenPlanIdIsNull()).willReturn(coverageEns);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .post("/company/benefit-plan")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(data))).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateBenefitPlan() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT_PLAN.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final AuthUser currentUser = getAuthUser();
    final Company company = new Company(currentUser.getCompanyId());
    BenefitPlan benefitPlan = new BenefitPlan();
    benefitPlan.setCompany(company);
    given(benefitPlanService.findBenefitPlanById("1")).willReturn(benefitPlan);
    final NewBenefitPlanWrapperDto data = new NewBenefitPlanWrapperDto();
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .patch("/company/benefit-plans/1")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(data))).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetBenefitPlanTypes() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT_PLAN.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/benefit-plan-types")
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetBenefitPlanPreview() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT_PLAN.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/benefit-plan-types/1/plan-preview")
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void updateBenefitPlanUsers() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT_PLAN.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    List<BenefitPlanUserCreateDto> benefitPlanUsers = new ArrayList<>();
    BenefitPlanUserCreateDto benefitPlanUserCreateDto = new BenefitPlanUserCreateDto();
    benefitPlanUserCreateDto.setId("2");
    benefitPlanUsers.add(benefitPlanUserCreateDto);
    final AuthUser currentUser = getAuthUser();
    final Company company = new Company(currentUser.getCompanyId());
    BenefitPlan benefitPlan = new BenefitPlan();
    benefitPlan.setCompany(company);
    given(benefitPlanService.findBenefitPlanById("1")).willReturn(benefitPlan);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .patch("/company/benefit-plan/1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(benefitPlanUsers))).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetEnrolledBenefitNumber() throws Exception {
    setPermission(Permission.Name.VIEW_SELF_BENEFITS.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());
    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    given(benefitPlanService.getBenefitSummary(getAuthUser().getId())).willReturn(new BenefitSummaryDto());
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/my-benefit/" + getAuthUser().getId() + "/benefit-summary")
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetUserBenefitPlans() throws Exception {
    setPermission(Permission.Name.VIEW_SELF_BENEFITS.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());
    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/users/"+getAuthUser().getId()+"/benefit-plans")
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetBenefitPlanDetail() throws Exception {
    setPermission(Permission.Name.VIEW_SELF_BENEFITS.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final AuthUser currentUser = getAuthUser();
    final Company company = new Company(currentUser.getCompanyId());
    BenefitPlan benefitPlan = new BenefitPlan();
    benefitPlan.setCompany(company);
    given(benefitPlanService.findBenefitPlanById("1")).willReturn(benefitPlan);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/benefit-plans/1/plan-detail")
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }


  @Test
  void testGetUserAvailableBenefitPlans() throws Exception {
    setPermission(Permission.Name.VIEW_SELF_BENEFITS.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final AuthUser currentUser = getAuthUser();
    final User targetUser = new User();
    final Company company = new Company(currentUser.getCompanyId());
    targetUser.setCompany(company);
    targetUser.setId(currentUser.getId());
    given(userService.findById(currentUser.getId())).willReturn(targetUser);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/users/"+getAuthUser().getId()+"/benefit-info")
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testDeleteBenefitPlan() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT_PLAN.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final AuthUser currentUser = getAuthUser();
    final Company company = new Company(currentUser.getCompanyId());
    BenefitPlan benefitPlan = new BenefitPlan();
    benefitPlan.setCompany(company);
    given(benefitPlanService.findBenefitPlanById("1")).willReturn(benefitPlan);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .delete("/company/benefit-plans/1")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetBenefitPlan() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT_PLAN.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final AuthUser currentUser = getAuthUser();
    final Company company = new Company(currentUser.getCompanyId());
    BenefitPlan benefitPlan = new BenefitPlan();
    benefitPlan.setCompany(company);
    given(benefitPlanService.findBenefitPlanById("1")).willReturn(benefitPlan);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/benefit-plans/1")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateSelectedBenefitEnrollmentInfo() throws Exception {
    setPermission(Permission.Name.EDIT_SELF.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    List<SelectedEnrollmentInfoDto> selectedInfos = new ArrayList<>();
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .patch("/company/users/benefit-enrollment")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(selectedInfos))).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testConfirmBenefitEnrollmentInfo() throws Exception {
    setPermission(Permission.Name.EDIT_SELF.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    List<SelectedEnrollmentInfoDto> selectedInfos = new ArrayList<>();
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .patch("/company/users/benefit-confirmation")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(selectedInfos))).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testHasConfirmation() throws Exception {
    setPermission(Permission.Name.VIEW_SELF_BENEFITS.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/users/benefit-plans/has-confirmation")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testHasConfirmationWithEmployeeId() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT_PLAN.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/users/benefit-plans/1/has-confirmation")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testUpdateTimeOffPolicyEmployeesInfo() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT_PLAN.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    List<BenefitPlanUserCreateDto> unSelectedEmployees = new ArrayList<>();
    final AuthUser currentUser = getAuthUser();
    final Company company = new Company(currentUser.getCompanyId());
    BenefitPlan benefitPlan = new BenefitPlan();
    benefitPlan.setCompany(company);
    given(benefitPlanService.findBenefitPlanById("1")).willReturn(benefitPlan);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .patch("/company/benefit-plan/employees/1")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)
        .content(JsonUtil.formatToString(unSelectedEmployees))).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetEmployeesByBenefitPlanId() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT_PLAN.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final AuthUser currentUser = getAuthUser();
    final Company company = new Company(currentUser.getCompanyId());
    BenefitPlan benefitPlan = new BenefitPlan();
    benefitPlan.setCompany(company);
    given(benefitPlanService.findBenefitPlanById("1")).willReturn(benefitPlan);

    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/benefit-plan/1/users")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetCoveragesByBenefitPlanId() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT_PLAN.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/benefit-plan/coverages")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetBenefitPlanReport() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT_PLAN.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/benefit-plan/1/reports")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetBenefitPlans() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT_PLAN.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/benefit-plan/1/plans")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetAllUsersByBenefitPlanId() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT_PLAN.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final AuthUser currentUser = getAuthUser();
    final Company company = new Company(currentUser.getCompanyId());
    BenefitPlan benefitPlan = new BenefitPlan();
    benefitPlan.setCompany(company);
    given(benefitPlanService.findBenefitPlanById("1")).willReturn(benefitPlan);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/benefit-plan/1/selectedUsers")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void testGetAllCoveragesByBenefitPlanId() throws Exception {
    setPermission(Permission.Name.MANAGE_BENEFIT_PLAN.name());
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", "Bearer " + JwtUtil.generateRsaToken());
    final AuthUser currentUser = getAuthUser();
    final Company company = new Company(currentUser.getCompanyId());
    BenefitPlan benefitPlan = new BenefitPlan();
    benefitPlan.setCompany(company);
    given(benefitPlanService.findBenefitPlanById("1")).willReturn(benefitPlan);
    final MvcResult response = mockMvc.perform(MockMvcRequestBuilders
        .get("/company/benefit-plan/1/allCoverages")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(httpHeaders)).andReturn();
    assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
