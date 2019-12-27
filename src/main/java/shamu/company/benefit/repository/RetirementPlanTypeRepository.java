package shamu.company.benefit.repository;

import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.RetirementPlanType;
import shamu.company.common.repository.BaseRepository;

public interface RetirementPlanTypeRepository extends BaseRepository<RetirementPlanType, String> {

  RetirementPlanType findByBenefitPlan(BenefitPlan benefitPlan);
}
