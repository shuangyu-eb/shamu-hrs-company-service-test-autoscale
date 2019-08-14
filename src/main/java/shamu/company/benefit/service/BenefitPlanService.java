package shamu.company.benefit.service;

import java.util.List;
import shamu.company.benefit.dto.BenefitPlanClusterDto;
import shamu.company.benefit.dto.BenefitPlanCoverageDto;
import shamu.company.benefit.dto.BenefitPlanCreateDto;
import shamu.company.benefit.dto.BenefitPlanUserCreateDto;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.company.entity.Company;

public interface BenefitPlanService {

  BenefitPlan createBenefitPlan(BenefitPlanCreateDto benefitPlanCreateDto,
      List<BenefitPlanCoverageDto> benefitPlanCoverageDtoList,
      List<BenefitPlanUserCreateDto> benefitPlanUserCreateDtoList, Company company);

  BenefitPlan findBenefitPlanById(Long id);

  void save(BenefitPlan benefitPlan);

  List<BenefitPlanClusterDto> getBenefitPlanCluster(Company company);

  void updateBenefitPlanUsers(Long benefitPlanId, List<BenefitPlanUserCreateDto> benefitPlanUsers);
}
