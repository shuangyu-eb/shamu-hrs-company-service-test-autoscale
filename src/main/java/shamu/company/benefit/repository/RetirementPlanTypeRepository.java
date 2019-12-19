package shamu.company.benefit.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.RetirementPlanType;
import shamu.company.common.repository.BaseRepository;

public interface RetirementPlanTypeRepository extends BaseRepository<RetirementPlanType, String> {

  RetirementPlanType findByBenefitPlan(BenefitPlan benefitPlan);

  @Transactional
  @Modifying
  @Query(value =
      "delete from retirement_plans_types"
          + " where benefit_plan_id = ?1 ",
      nativeQuery = true)
  void deleteByBenefitPlanId(String planId);
}
