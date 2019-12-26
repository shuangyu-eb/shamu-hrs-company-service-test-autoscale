package shamu.company.benefit.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.benefit.repository.BenefitPlanTypeRepository;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.utils.ReflectionUtil;

@RestApiController
public class BenefitPlanTypeRestController {

  private final BenefitPlanTypeRepository benefitPlanTypeRepository;

  @Autowired
  public BenefitPlanTypeRestController(final BenefitPlanTypeRepository benefitPlanTypeRepository) {
    this.benefitPlanTypeRepository = benefitPlanTypeRepository;
  }

  @GetMapping("all-benefit-plan-types")
  public List<CommonDictionaryDto> getBenefitPlanTypes() {

    final List<BenefitPlanType> planTypes = benefitPlanTypeRepository
        .findAll();
    return ReflectionUtil.convertTo(planTypes, CommonDictionaryDto.class);
  }
}
