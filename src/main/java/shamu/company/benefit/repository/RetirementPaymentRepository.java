package shamu.company.benefit.repository;

import java.util.List;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.RetirementPayment;
import shamu.company.common.repository.BaseRepository;

public interface RetirementPaymentRepository extends BaseRepository<RetirementPayment, String> {

  @Override
  RetirementPayment save(RetirementPayment retirementPayment);

  List<RetirementPayment> findAllByBenefitPlanAndUserIsNotNull(BenefitPlan benefitPlan);

  RetirementPayment findByBenefitPlanAndUserIsNull(BenefitPlan benefitPlan);

  Long countByBenefitPlanIdAndUserIsNotNull(String benefitPlanId);

}
