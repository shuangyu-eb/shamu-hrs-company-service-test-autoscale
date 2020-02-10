package shamu.company.benefit.controller;

import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.benefit.dto.BenefitPlanCoveragesDto;
import shamu.company.benefit.dto.BenefitPlanDetailDto;
import shamu.company.benefit.dto.BenefitPlanDto;
import shamu.company.benefit.dto.BenefitPlanPreviewDto;
import shamu.company.benefit.dto.BenefitPlanRelatedUserListDto;
import shamu.company.benefit.dto.BenefitPlanReportSummaryDto;
import shamu.company.benefit.dto.BenefitPlanTypeDto;
import shamu.company.benefit.dto.BenefitPlanUpdateDto;
import shamu.company.benefit.dto.BenefitPlanUserCreateDto;
import shamu.company.benefit.dto.BenefitSummaryDto;
import shamu.company.benefit.dto.NewBenefitPlanWrapperDto;
import shamu.company.benefit.dto.SelectedEnrollmentInfoDto;
import shamu.company.benefit.dto.UserBenefitPlanDto;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.benefit.entity.mapper.BenefitPlanMapper;
import shamu.company.benefit.entity.mapper.BenefitPlanTypeMapper;
import shamu.company.benefit.service.BenefitPlanService;
import shamu.company.benefit.service.BenefitPlanTypeService;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.common.validation.constraints.FileValidate;
import shamu.company.employee.dto.SelectFieldInformationDto;

@RestApiController
@Validated
public class BenefitPlanRestController extends BaseRestController {

  private final BenefitPlanService benefitPlanService;

  private final BenefitPlanTypeService benefitPlanTypeService;

  private final BenefitPlanMapper benefitPlanMapper;

  private final BenefitPlanTypeMapper benefitPlanTypeMapper;

  public BenefitPlanRestController(
      final BenefitPlanService benefitPlanService,
      final BenefitPlanMapper benefitPlanMapper,
      final BenefitPlanTypeService benefitPlanTypeService,
      final BenefitPlanTypeMapper benefitPlanTypeMapper) {
    this.benefitPlanService = benefitPlanService;
    this.benefitPlanMapper = benefitPlanMapper;
    this.benefitPlanTypeService = benefitPlanTypeService;
    this.benefitPlanTypeMapper = benefitPlanTypeMapper;
  }

  @GetMapping("benefit-plan-types/default")
  @PreAuthorize("hasAuthority('MANAGE_BENEFIT_PLAN')")
  public List<SelectFieldInformationDto> findAllBenefitPlanTypes() {
    final List<BenefitPlanType> benefitPlanTypes = benefitPlanTypeService.findAllBenefitPlanTypes();
    return benefitPlanTypeMapper.convertAllToDefaultBenefitPlanTypeDtos(benefitPlanTypes);
  }

  @PostMapping("benefit-plan")
  @PreAuthorize("hasAuthority('MANAGE_BENEFIT_PLAN')")
  public BenefitPlanDto createBenefitPlan(@RequestBody final NewBenefitPlanWrapperDto data) {
    return benefitPlanService.createBenefitPlan(data, findCompanyId());
  }

  @PatchMapping("benefit-plans/{id}")
  @PreAuthorize("hasPermission(#id,'BENEFIT_PLAN', 'MANAGE_BENEFIT_PLAN')")
  public BenefitPlanDto updateBenefitPlan(
      @RequestBody final NewBenefitPlanWrapperDto data, @PathVariable final String id) {
    return benefitPlanService.updateBenefitPlan(data, id, findCompanyId());
  }

  @PostMapping("benefit-plan/{id}/document")
  @PreAuthorize("hasPermission(#id,'BENEFIT_PLAN', 'MANAGE_BENEFIT_PLAN')")
  public ResponseEntity uploadBenefitPlanDocument(
      @PathVariable final String id,
      @RequestParam("file")
          @FileValidate(
              maxSize = 10 * 1024 * 1024,
              fileType = {"PDF"})
          final List<MultipartFile> files) {
    benefitPlanService.saveBenefitPlanDocuments(id, files);
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping("benefit-plan-types")
  public List<BenefitPlanTypeDto> getBenefitPlanTypes() {
    return benefitPlanService.getBenefitPlanTypesAndNum(findCompanyId());
  }

  @GetMapping("benefit-plan-types/{planTypeId}/plan-preview")
  public List<BenefitPlanPreviewDto> getBenefitPlanPreview(@PathVariable final String planTypeId) {
    return benefitPlanService.getBenefitPlanPreview(findCompanyId(), planTypeId);
  }

  @PatchMapping("benefit-plan/{benefitPlanId}/users")
  @PreAuthorize("hasPermission(#benefitPlanId,'BENEFIT_PLAN', 'MANAGE_BENEFIT_PLAN')")
  public void updateBenefitPlanUsers(
      @PathVariable final String benefitPlanId,
      @RequestBody final List<BenefitPlanUserCreateDto> benefitPlanUsers) {
    benefitPlanService.updateBenefitPlanUsers(benefitPlanId, benefitPlanUsers);
  }

  @GetMapping("my-benefit/{userId}/benefit-summary")
  @PreAuthorize("hasPermission(#userId,'USER','VIEW_SELF_BENEFITS')")
  public BenefitSummaryDto getEnrolledBenefitNumber(@PathVariable final String userId) {
    return benefitPlanService.getBenefitSummary(userId);
  }

  @GetMapping("users/{userId}/benefit-plans")
  @PreAuthorize("hasPermission(#userId,'USER','VIEW_SELF_BENEFITS')")
  public List<UserBenefitPlanDto> getUserBenefitPlans(@PathVariable final String userId) {
    return benefitPlanService.getUserBenefitPlans(userId);
  }

  @GetMapping("benefit-plans/{id}/plan-detail")
  @PreAuthorize("hasPermission(#id,'BENEFIT_PLAN','VIEW_SELF_BENEFITS')")
  public BenefitPlanDetailDto getBenefitPlanDetail(@PathVariable final String id) {
    final BenefitPlan benefitPlan = benefitPlanService.findBenefitPlanById(id);
    return benefitPlanMapper.concertTo(benefitPlan);
  }

  @GetMapping("users/{userId}/benefit-info")
  @PreAuthorize("hasPermission(#userId,'USER','VIEW_SELF_BENEFITS')")
  public List<UserBenefitPlanDto> getUserAvailableBenefitPlans(@PathVariable final String userId) {
    return benefitPlanService.getUserAvailableBenefitPlans(userId);
  }

  @DeleteMapping("benefit-plans/{id}")
  @PreAuthorize("hasPermission(#id,'BENEFIT_PLAN', 'MANAGE_BENEFIT_PLAN')")
  public void deleteBenefitPlan(@PathVariable final String id) {
    benefitPlanService.deleteBenefitPlanByPlanId(id);
  }

  @GetMapping("benefit-plans/{id}")
  @PreAuthorize("hasPermission(#id,'BENEFIT_PLAN', 'MANAGE_BENEFIT_PLAN')")
  public BenefitPlanUpdateDto getBenefitPlan(@PathVariable final String id) {
    return benefitPlanService.getBenefitPlanByPlanId(id);
  }

  @PatchMapping("users/benefit-enrollment")
  @PreAuthorize("hasAuthority('EDIT_SELF')")
  public void updateSelectedBenefitEnrollmentInfo(
      @RequestBody final List<SelectedEnrollmentInfoDto> selectedInfos) {
    final String userId = findAuthUser().getId();
    benefitPlanService.updateUserBenefitPlanEnrollmentInfo(userId, selectedInfos, findCompanyId());
  }

  @PatchMapping("users/benefit-confirmation")
  @PreAuthorize("hasAuthority('EDIT_SELF')")
  public HttpEntity confirmBenefitEnrollmentInfo(
      @RequestBody final List<SelectedEnrollmentInfoDto> selectedInfos) {
    final String userId = findAuthUser().getId();
    benefitPlanService.confirmBenefitPlanEnrollment(userId, selectedInfos, findCompanyId());
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping("users/benefit-plans/has-confirmation")
  @PreAuthorize("hasAuthority('VIEW_SELF_BENEFITS')")
  public boolean hasConfirmation() {
    final String userId = findAuthUser().getId();
    return benefitPlanService.isConfirmed(userId);
  }

  @PatchMapping("benefit-plan/employees/{benefitPlanId}")
  @PreAuthorize("hasPermission(#benefitPlanId, 'BENEFIT_PLAN', 'MANAGE_BENEFIT_PLAN')")
  public BenefitPlanRelatedUserListDto updateTimeOffPolicyEmployeesInfo(
      @PathVariable final String benefitPlanId,
      @RequestBody final List<BenefitPlanUserCreateDto> unSelectedEmployees) {
    return benefitPlanService.updateBenefitPlanEmployees(
        unSelectedEmployees, benefitPlanId, findCompanyId());
  }

  @GetMapping("benefit-plan/{benefitPlanId}/users")
  @PreAuthorize("hasPermission(#benefitPlanId, 'BENEFIT_PLAN', 'MANAGE_BENEFIT_PLAN')")
  public BenefitPlanRelatedUserListDto getEmployeesByBenefitPlanId(
      @PathVariable final String benefitPlanId) {
    return benefitPlanService.findRelatedUsersByBenefitPlan(benefitPlanId, findCompanyId());
  }

  @GetMapping("benefit-plan/coverages")
  @PreAuthorize("hasAuthority('MANAGE_BENEFIT_PLAN')")
  public BenefitPlanCoveragesDto getCoveragesByBenefitPlanId() {
    return benefitPlanService.findCoveragesByBenefitPlanId();
  }

  @GetMapping("benefit-plan/{planTypeName}/reports")
  @PreAuthorize("hasAuthority('MANAGE_BENEFIT_PLAN')")
  public List<BenefitPlanReportSummaryDto> getBenefitPlanReport(
      @PathVariable final String planTypeName) {
    return benefitPlanService.getBenefitPlanReport(planTypeName, findCompanyId());
  }
}
