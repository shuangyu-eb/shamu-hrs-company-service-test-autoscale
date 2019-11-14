package shamu.company.benefit.controller;

import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.benefit.dto.BenefitPlanClusterDto;
import shamu.company.benefit.dto.BenefitPlanCoverageDto;
import shamu.company.benefit.dto.BenefitPlanCreateDto;
import shamu.company.benefit.dto.BenefitPlanDto;
import shamu.company.benefit.dto.BenefitPlanUserCreateDto;
import shamu.company.benefit.dto.NewBenefitPlanWrapperDto;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.mapper.BenefitPlanMapper;
import shamu.company.benefit.service.BenefitPlanService;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.common.validation.constraints.FileValidate;
import shamu.company.s3.AccessType;
import shamu.company.s3.AwsUtil;

@RestApiController
@Validated
public class BenefitPlanRestController extends BaseRestController {


  private final BenefitPlanService benefitPlanService;

  private final AwsUtil awsUtil;

  private final BenefitPlanMapper benefitPlanMapper;


  public BenefitPlanRestController(
      final BenefitPlanService benefitPlanService,
      final AwsUtil awsUtil,
      final BenefitPlanMapper benefitPlanMapper) {
    this.benefitPlanService = benefitPlanService;
    this.awsUtil = awsUtil;
    this.benefitPlanMapper = benefitPlanMapper;
  }

  @PostMapping("benefit-plan")
  @PreAuthorize("hasAuthority('MANAGE_BENEFIT_PLAN')")
  public BenefitPlanDto createBenefitPlan(@RequestBody final NewBenefitPlanWrapperDto data) {
    final BenefitPlanCreateDto benefitPlanCreateDto = data.getBenefitPlan();

    final List<BenefitPlanCoverageDto> benefitPlanCoverageDtoList = data.getCoverages();

    final List<BenefitPlanUserCreateDto> benefitPlanUserCreateDtoList = data.getSelectedEmployees();

    final BenefitPlan benefitPlan = benefitPlanService
        .createBenefitPlan(benefitPlanCreateDto, benefitPlanCoverageDtoList,
            benefitPlanUserCreateDtoList,
            getCompanyId());
    return benefitPlanMapper.convertToBenefitPlanDto(benefitPlan);
  }

  @PostMapping("benefit-plan/{id}/document")
  @PreAuthorize("hasPermission(#id,'BENEFIT_PLAN', 'MANAGE_BENEFIT_PLAN')")
  public void uploadBenefitPlanDocument(@PathVariable final String id,
      @RequestParam("file")
      //TODO: Need an appropriate file size.
      @FileValidate(maxSize = 10 * 1024 * 1024, fileType = {"PDF"})
      final MultipartFile document) throws IOException {
    final String path = awsUtil.uploadFile(document, AccessType.Private);

    if (Strings.isBlank(path)) {
      return;
    }

    final BenefitPlan benefitPlan = benefitPlanService.findBenefitPlanById(id);
    benefitPlan.setDocumentUrl(path);
    benefitPlanService.save(benefitPlan);
  }

  @GetMapping("benefit-plan-clusters")
  public List<BenefitPlanClusterDto> getBenefitPlanClusters() {
    return benefitPlanService.getBenefitPlanCluster(getCompanyId());
  }

  @PatchMapping("benefit-plan/{benefitPlanId}/users")
  @PreAuthorize("hasPermission(#benefitPlanId,'BENEFIT_PLAN', 'MANAGE_BENEFIT_PLAN')")
  public void updateBenefitPlanUsers(@PathVariable final String benefitPlanId,
      @RequestBody final List<BenefitPlanUserCreateDto> benefitPlanUsers) {
    benefitPlanService.updateBenefitPlanUsers(benefitPlanId, benefitPlanUsers);
  }
}
