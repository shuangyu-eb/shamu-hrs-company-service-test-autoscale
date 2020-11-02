package shamu.company.benefit.repository;

import java.util.List;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.RetirementPayment;
import shamu.company.common.repository.BaseRepository;
import shamu.company.user.entity.User;

public interface RetirementPaymentRepository extends BaseRepository<RetirementPayment, String> {

  @Override
  RetirementPayment save(RetirementPayment retirementPayment);

  List<RetirementPayment> findAllByBenefitPlanAndUserIsNotNull(BenefitPlan benefitPlan);

  RetirementPayment findByBenefitPlanAndUserIsNull(BenefitPlan benefitPlan);

  Long countByBenefitPlanIdAndUserIsNotNull(String benefitPlanId);

  List<RetirementPayment> findAllByUserId(String userId);

}
