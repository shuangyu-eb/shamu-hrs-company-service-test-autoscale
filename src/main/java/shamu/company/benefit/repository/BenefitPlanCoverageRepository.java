package shamu.company.benefit.repository;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.benefit.entity.BenefitPlanCoverage;
import shamu.company.common.repository.BaseRepository;

public interface BenefitPlanCoverageRepository extends BaseRepository<BenefitPlanCoverage, String> {

  @Query(
      value =
          "select sum(employee_cost) "
              + "from benefit_plan_coverages "
              + "where id in ("
              + "select coverage_id from benefit_plans_users "
              + "where user_id = ?1 "
              + "and enrolled = 1)",
      nativeQuery = true)
  BigDecimal getBenefitCostByUserId(String userId);

  List<BenefitPlanCoverage> findAllByBenefitPlanId(String planId);

  @Transactional
  @Modifying
  @Query(value =
      "update benefit_plan_coverages"
          + " set deleted_at = current_timestamp"
          + " where benefit_plan_id = ?1",
      nativeQuery = true)
  void deleteAllByBenefitPlanId(String planId);
}
