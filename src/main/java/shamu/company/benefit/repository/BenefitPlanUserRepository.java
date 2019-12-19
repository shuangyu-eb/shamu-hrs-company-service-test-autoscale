package shamu.company.benefit.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanUser;
import shamu.company.common.repository.BaseRepository;

public interface BenefitPlanUserRepository extends BaseRepository<BenefitPlanUser, String> {

  List<BenefitPlanUser> findByUserIdAndEnrolledIsTrue(String userId);

  List<BenefitPlanUser> findAllByUserId(String userId);

  List<BenefitPlanUser> findAllByBenefitPlan(BenefitPlan benefitPlan);

  @Query(value = "select count(1) from users u "
      + "where u.id "
      + "not in (select bpu.user_id from benefit_plans_users bpu "
      + "where bpu.benefit_plan_id = ?2) "
      + "and u.company_id = ?1 ", nativeQuery = true)
  Long getEligibleEmployeeNumber(String companyId, String benefitPlanId);

  Long countByBenefitPlanIdAndEnrolled(String benefitPlanId, Boolean enrolled);

  Long countByUserIdAndEnrolled(String userId, Boolean enrolled);

  Optional<BenefitPlanUser> findByUserIdAndBenefitPlanId(String userId, String benefitPlanId);

  @Transactional
  @Modifying
  @Query(value =
      "delete from benefit_plans_users"
          + " where benefit_plan_id = ?1 ",
      nativeQuery = true)
  void deleteAllByBenefitPlanId(String planId);
}
