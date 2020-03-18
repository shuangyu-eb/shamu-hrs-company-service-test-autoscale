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

  @Query(
      value =
          "select count(distinct(user_id)) from benefit_plans_users "
              + "where benefit_plan_id = unhex(?1) ",
      nativeQuery = true)
  Long getEligibleEmployeeNumber(String benefitPlanId);

  Long countByBenefitPlanIdAndConfirmedIsTrue(String benefitPlanId);

  Long countByUserIdAndEnrolled(String userId, Boolean enrolled);

  Long countByBenefitPlanIdAndEnrolledIsTrue(String benefitPlanId);

  Optional<BenefitPlanUser> findByUserIdAndBenefitPlanId(String userId, String benefitPlanId);

  @Query(
      value =
          "select count(distinct(user_id)) from benefit_plans_users "
              + "where hex(benefit_plan_id) in ?1 and confirmed is true "
              + "and enrolled is true",
      nativeQuery = true)
  Long getEmployeesEnrolledNumber(List<String> benefitPlanIds);

  @Query(
      value =
          "select count(distinct(bpu.user_id)) from benefit_plans_users bpu "
              + "left join benefit_plan_coverages bpc "
              + "on bpu.coverage_id = bpc.id "
              + "left join benefit_coverages bc "
              + "on bpc.benefit_coverage_id = bc.id "
              + "where hex(bpu.benefit_plan_id) in ?1 and "
              + "bpu.confirmed is true and bpu.enrolled is true and bc.id = unhex(?2)",
      nativeQuery = true)
  Long getEmployeesEnrolledNumberByCoverageId(List<String> benefitPlanIds, String coverageId);
}
