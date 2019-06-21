package shamu.company.benefit.controller;

import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.benefit.dto.BenefitPlanClusterDto;
import shamu.company.benefit.dto.BenefitPlanDto;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.pojo.BenefitPlanCoveragePojo;
import shamu.company.benefit.pojo.BenefitPlanPojo;
import shamu.company.benefit.pojo.BenefitPlanUserPojo;
import shamu.company.benefit.pojo.NewBenefitPlanWrapperPojo;
import shamu.company.benefit.service.BenefitPlanService;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.company.entity.Company;
import shamu.company.hashids.HashidsFormat;
import shamu.company.utils.AwsUtil;

@RestApiController
public class BenefitPlanRestController extends BaseRestController {


  private final BenefitPlanService benefitPlanService;

  private final AwsUtil awsUtil;

  public BenefitPlanRestController(
      BenefitPlanService benefitPlanService,
      AwsUtil awsUtil) {
    this.benefitPlanService = benefitPlanService;
    this.awsUtil = awsUtil;
  }

  @PostMapping("benefit-plan")
  @PreAuthorize("hasAuthority('MANAGE_BENEFIT_PLAN')")
  public BenefitPlanDto createBenefitPlan(@RequestBody NewBenefitPlanWrapperPojo data) {
    BenefitPlanPojo benefitPlanPojo = data.getBenefitPlan();

    List<BenefitPlanCoveragePojo> benefitPlanCoveragePojoList = data.getCoverages();

    List<BenefitPlanUserPojo> benefitPlanUserPojoList = data.getSelectedEmployees();

    Company company = this.getCompany();

    BenefitPlan benefitPlan =  benefitPlanService
        .createBenefitPlan(benefitPlanPojo, benefitPlanCoveragePojoList, benefitPlanUserPojoList,
            company);
    return new BenefitPlanDto(benefitPlan);
  }

  @PostMapping("benefit-plan/{id}/document")
  @PreAuthorize("hasPermission(#id,'BENEFIT_PLAN', 'MANAGE_BENEFIT_PLAN')")
  public void uploadBenefitPlanDocument(@PathVariable @HashidsFormat Long id,
      @RequestParam("file") MultipartFile document) throws IOException {
    String path = awsUtil.uploadFile(document);

    if (Strings.isBlank(path)) {
      return;
    }

    BenefitPlan benefitPlan = benefitPlanService.findBenefitPlanById(id);
    benefitPlan.setDocumentUrl(path);
    benefitPlanService.save(benefitPlan);
  }

  @GetMapping("benefit-plan-clusters")
  public List<BenefitPlanClusterDto> getBenefitPlanClusters() {
    return benefitPlanService.getBenefitPlanCluster(this.getCompany());
  }

  @PatchMapping("benefit-plan/{benefitPlanId}/users")
  public void updateBenefitPlanUsers(@PathVariable @HashidsFormat Long benefitPlanId,
      @RequestBody List<BenefitPlanUserPojo> benefitPlanUsers) {
    benefitPlanService.updateBenefitPlanUsers(benefitPlanId, benefitPlanUsers);
  }
}
