package shamu.company.benefit.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import shamu.company.benefit.dto.BenefitCoveragesDto;
import shamu.company.benefit.dto.BenefitReportCoveragesDto;
import shamu.company.benefit.entity.BenefitCoverages;
import shamu.company.benefit.entity.BenefitPlanCoverage;
import shamu.company.common.repository.BaseRepository;

public interface BenefitPlanCoverageRepository extends BaseRepository<BenefitPlanCoverage, String> {

  @Query(
      value =
          "select sum(employee_cost) "
              + "from benefit_plan_coverages "
              + "where id in ("
              + "select coverage_id from benefit_plans_users "
              + "where user_id = unhex(?1) "
              + "and enrolled is true)",
      nativeQuery = true)
  BigDecimal getBenefitCostByUserId(String userId);

  List<BenefitPlanCoverage> findAllByBenefitPlanId(String planId);

  @Override
  Optional<BenefitPlanCoverage> findById(String id);

  @Query(
      value =
          "select sum(bpc.employer_cost) from benefit_plans_users bpu "
              + "left join benefit_plan_coverages bpc "
              + "on bpu.coverage_id = bpc.id "
              + "where hex(bpu.benefit_plan_id) in ?1 and bpu.confirmed is not null",
      nativeQuery = true)
  BigDecimal getCompanyCost(List<String> benefitPlanIds);

  @Query(
      value =
          "select sum(bpc.employer_cost) from benefit_plans_users bpu "
              + "left join benefit_plan_coverages bpc "
              + "on bpu.coverage_id = bpc.id "
              + "left join benefit_coverages bc "
              + "on bpc.benefit_coverage_id = bc.id "
              + "where hex(bpu.benefit_plan_id) in ?1 "
              + "and bpu.confirmed is not null and bc.id = unhex(?2)",
      nativeQuery = true)
  BigDecimal getCompanyCostByCoverageId(List<String> benefitPlanIds, String coverageId);

  @Query(
      value =
          "select sum(bpc.employee_cost) from benefit_plans_users bpu "
              + "left join benefit_plan_coverages bpc "
              + "on bpu.coverage_id = bpc.id "
              + "where hex(bpu.benefit_plan_id) in ?1 and bpu.confirmed is not null",
      nativeQuery = true)
  BigDecimal getEmployeeCost(List<String> benefitPlanIds);

  @Query(
      value =
          "select sum(bpc.employee_cost) from benefit_plans_users bpu "
              + "left join benefit_plan_coverages bpc "
              + "on bpu.coverage_id = bpc.id "
              + "left join benefit_coverages bc "
              + "on bpc.benefit_coverage_id = bc.id "
              + "where hex(bpu.benefit_plan_id) in ?1 "
              + "and bpu.confirmed is not null and bc.id = unhex(?2)",
      nativeQuery = true)
  BigDecimal getEmployeeCostByCoverageId(List<String> benefitPlanIds, String coverageId);

  @Query(
      value =
          "select new shamu.company.benefit.dto.BenefitReportCoveragesDto(bc.id, bc.name) "
              + "from BenefitPlanCoverage bpc "
              + "left join BenefitCoverages bc "
              + "on bpc.benefitCoverage.id = bc.id "
              + "where bpc.benefitPlanId in ?1")
  List<BenefitReportCoveragesDto> getBenefitReportCoverages(List<String> benefitPlanIds);

  BenefitPlanCoverage getByBenefitPlanIdAndBenefitCoverageId(
      String benefitPlanId, String benefitCoverageId);

  @Query(
      value =
          "select new shamu.company.benefit.dto.BenefitCoveragesDto(bpc.id, bc.name) "
              + "from BenefitPlanCoverage bpc "
              + "left join BenefitCoverages bc "
              + "on bpc.benefitCoverage.id = bc.id "
              + "where bpc.benefitPlanId = ?1")
  List<BenefitCoveragesDto> getBenefitPlanCoveragesByPlanId(String benefitPlanId);
}
