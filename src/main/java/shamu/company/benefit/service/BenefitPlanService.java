package shamu.company.benefit.service;

import java.util.List;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.pojo.BenefitPlanCoveragePojo;
import shamu.company.benefit.pojo.BenefitPlanPojo;
import shamu.company.benefit.pojo.BenefitPlanUserPojo;
import shamu.company.company.entity.Company;

public interface BenefitPlanService {

  BenefitPlan createBenefitPlan(BenefitPlanPojo benefitPlanPojo,
      List<BenefitPlanCoveragePojo> benefitPlanCoveragePojoList,
      List<BenefitPlanUserPojo> benefitPlanUserPojoList, Company company);

  BenefitPlan findBenefitPlanById(Long id);

  void save(BenefitPlan benefitPlan);
}
