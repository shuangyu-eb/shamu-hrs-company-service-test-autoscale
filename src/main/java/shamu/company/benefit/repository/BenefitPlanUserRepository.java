package shamu.company.benefit.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanUser;
import shamu.company.common.repository.BaseRepository;

public interface BenefitPlanUserRepository extends BaseRepository<BenefitPlanUser, String> {

  List<BenefitPlanUser> findByUserIdAndEnrolledIsTrue(String userId);

  List<BenefitPlanUser> findByUserIdAndConfirmedIsTrue(String userId);

  List<BenefitPlanUser> findAllByUserId(String userId);

  List<BenefitPlanUser> findAllByBenefitPlan(BenefitPlan benefitPlan);

  List<BenefitPlanUser> findAllByBenefitPlanId(String benefitPlanId);

  @Query(value = "select count(distinct(user_id)) from benefit_plans_users "
      + "where benefit_plan_id = unhex(?1) ", nativeQuery = true)
  Long getEligibleEmployeeNumber(String benefitPlanId);

  Long countByBenefitPlanIdAndConfirmedIsTrue(String benefitPlanId);

  Long countByUserIdAndEnrolled(String userId, Boolean enrolled);

  Optional<BenefitPlanUser> findByUserIdAndBenefitPlanId(String userId, String benefitPlanId);
}
