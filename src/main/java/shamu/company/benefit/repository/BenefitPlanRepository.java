package shamu.company.benefit.repository;

import java.sql.Timestamp;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import shamu.company.benefit.dto.BenefitPlanPreviewDto;
import shamu.company.benefit.dto.BenefitPlanTypeDto;
import shamu.company.benefit.dto.BenefitReportPlansDto;
import shamu.company.benefit.dto.EnrollmentBreakdownDto;
import shamu.company.benefit.entity.BenefitDependentUserNamePojo;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanPreviewPojo;
import shamu.company.benefit.entity.EnrollmentBreakdownPojo;
import shamu.company.common.repository.BaseRepository;

public interface BenefitPlanRepository extends BaseRepository<BenefitPlan, String> {

  List<BenefitPlan> findBenefitPlanByCompanyId(String companyId);

  List<BenefitPlan> findBenefitPlanByIdAndCompanyId(String benefitPlanId, String companyId);

  List<BenefitPlan> findByBenefitPlanTypeIdAndCompanyIdOrderByNameAsc(
      String benefitPlanTypeId, String companyId);

  @Query(
      "select new shamu.company.benefit.dto.BenefitPlanTypeDto("
          + "bpt.id, bpt.name, count(bp.benefitPlanType.id) )"
          + " from shamu.company.benefit.entity.BenefitPlanType bpt"
          + " left join BenefitPlan bp"
          + " on bp.benefitPlanType.id = bpt.id"
          + " and bp.company.id = ?1"
          + " group by bpt.id"
          + " order by bpt.id")
  List<BenefitPlanTypeDto> findPlanTypeAndNumByCompanyIdOrderByTypeId(String companyId);

  BenefitPlan findBenefitPlanById(String planId);

  BenefitPlan findBenefitPlanByName(String planName);

  @Query(
      value =
          "select hex(bp.id) from benefit_plan_types bpt "
              + "left join benefit_plans bp "
              + "on bpt.id = bp.benefit_plan_type_id "
              + "where bpt.name = ?1 and bp.company_id = unhex(?2)",
      nativeQuery = true)
  List<String> getBenefitPlanIds(String name, String companyId);

  @Query(
      value =
          "select new shamu.company.benefit.dto.BenefitReportPlansDto(bp.id, bp.name) "
              + "from BenefitPlanType bpt "
              + "left join BenefitPlan bp "
              + "on bpt.id = bp.benefitPlanType.id "
              + "where bpt.name = ?1 and bp.company.id = ?2")
  List<BenefitReportPlansDto> getBenefitPlans(String name, String companyId);

  @Query(
      value =
          "select new shamu.company.benefit.dto.EnrollmentBreakdownDto(1L, "
              + "bpu.user.imageUrl,"
              + "bpu.user.userPersonalInformation.firstName,"
              + "bpu.user.userPersonalInformation.lastName,"
              + "bpu.benefitPlan.name,bc.name,bpu.benefitPlanDependents.size, "
              + "bpc.employeeCost, bpc.employerCost) "
              + "from BenefitPlanUser bpu "
              + "left join BenefitPlanCoverage bpc "
              + "on bpu.benefitPlanCoverage.id = bpc.id "
              + "left join BenefitCoverages bc "
              + "on bpc.benefitCoverage.id = bc.id "
              + "where bpu.benefitPlan.id in ?1 and bpu.confirmed = true and bpu.enrolled = true")
  List<EnrollmentBreakdownDto> getEnrollmentBreakdown(List<String> benefitPlanIds);

  @Query(
      value =
          "select new shamu.company.benefit.dto.EnrollmentBreakdownDto(1L, "
              + "bpu.user.imageUrl,"
              + "bpu.user.userPersonalInformation.firstName,"
              + "bpu.user.userPersonalInformation.lastName,"
              + "bpu.benefitPlan.name,bc.name,bpu.benefitPlanDependents.size, "
              + "bpc.employeeCost, bpc.employerCost) "
              + "from BenefitPlanUser bpu "
              + "left join BenefitPlanCoverage bpc "
              + "on bpu.benefitPlanCoverage.id = bpc.id "
              + "left join BenefitCoverages bc "
              + "on bpc.benefitCoverage.id = bc.id "
              + "where bpu.benefitPlan.id in ?1 and bpu.confirmed = true "
              + "and bpu.enrolled = true and bc.id = ?2 ")
  List<EnrollmentBreakdownDto> getEnrollmentBreakdown(
      List<String> benefitPlanIds, String coverageId);

  @Query(
      value =
          "select hex(bpu.id) as planUserId, 1 as number, u.image_url as imageUrl, "
              + "concat(upi.first_name,' ',upi.last_name) as fullName, "
              + "concat(upi.last_name,' ',upi.first_name) as orderName, "
              + "coalesce(bp.name) as plan, coalesce(bc.name) as coverage, "
              + "count(bpd.id) as dependents, coalesce(bpc.employee_cost) as companyCost, "
              + "coalesce(bpc.employer_cost) as employeeCost "
              + "from benefit_plans_users bpu "
              + "left join benefit_plans bp "
              + "on bpu.benefit_plan_id = bp.id "
              + "left join benefit_plan_coverages bpc "
              + "on bpu.coverage_id = bpc.id "
              + "left join benefit_coverages bc "
              + "on bpc.benefit_coverage_id = bc.id "
              + "left join users u "
              + "on bpu.user_id = u.id "
              + "left join user_personal_information upi "
              + "on upi.id = u.user_personal_information_id "
              + "left join benefit_plan_dependents bpd "
              + "on bpu.id = bpd.benefit_plans_users_id "
              + "where hex(bpu.benefit_plan_id) in ?1 and bpu.confirmed is true "
              + "and bpu.enrolled is true "
              + "group by bpu.id",
      countQuery =
          "select count(1) from benefit_plans_users bpu "
              + "where hex(bpu.benefit_plan_id) in ?1 and bpu.confirmed is true "
              + "and bpu.enrolled is true",
      nativeQuery = true)
  Page<EnrollmentBreakdownPojo> getEnrollmentBreakdownByCondition(
      List<String> ids, Pageable pageRequest);

  @Query(
      value =
          "select hex(bpu.id) as planUserId, 1 as number, u.image_url as imageUrl, "
              + "concat(upi.first_name,' ',upi.last_name) as fullName, "
              + "concat(upi.last_name,' ',upi.first_name) as orderName, "
              + "coalesce(bp.name) as plan, coalesce(bc.name) as coverage, "
              + "count(bpd.id) as dependents, coalesce(bpc.employee_cost) as companyCost, "
              + "coalesce(bpc.employer_cost) as employeeCost "
              + "from benefit_plans_users bpu "
              + "left join benefit_plans bp "
              + "on bpu.benefit_plan_id = bp.id "
              + "left join benefit_plan_coverages bpc "
              + "on bpu.coverage_id = bpc.id "
              + "left join benefit_coverages bc "
              + "on bpc.benefit_coverage_id = bc.id "
              + "left join users u "
              + "on bpu.user_id = u.id "
              + "left join user_personal_information upi "
              + "on upi.id = u.user_personal_information_id "
              + "left join benefit_plan_dependents bpd "
              + "on bpu.id = bpd.benefit_plans_users_id "
              + "where hex(bpu.benefit_plan_id) in ?1 "
              + "and bpu.confirmed is true and bpu.enrolled is true and bc.id = unhex(?2) "
              + "group by bpu.id",
      countQuery =
          "select count(1) from benefit_plans_users bpu "
              + "left join benefit_coverages bc "
              + "on bpu.benefit_plan_id = bc.benefit_plan_id "
              + "where hex(bpu.benefit_plan_id) in ?1 "
              + "and bpu.confirmed is true and bpu.enrolled is true and bc.id = unhex(?2)",
      nativeQuery = true)
  Page<EnrollmentBreakdownPojo> getEnrollmentBreakdownByConditionAndCoverageId(
      List<String> ids, String coverageId, Pageable pageRequest);

  @Query(
      value =
          "select hex(bpu.id) as planUserId, "
              + "concat(ud.first_name,' ',ud.last_name) as dependentUserName "
              + "from benefit_plans_users bpu "
              + "left join benefit_plan_dependents bpd "
              + "on bpu.id = bpd.benefit_plans_users_id "
              + "left join user_dependents ud "
              + "on ud.id = bpd.user_dependents_id "
              + "where hex(bpu.benefit_plan_id) in ?1 and bpu.confirmed is true "
              + "and bpu.enrolled is true",
      nativeQuery = true)
  List<BenefitDependentUserNamePojo> getDependentUserNameByPlanUserId(List<String> ids);

  @Query(
      value =
          "select hex(bpu.id) as planUserId, "
              + "concat(ud.first_name,' ',ud.last_name) as dependentUserName "
              + "from benefit_plans_users bpu "
              + "left join benefit_plan_coverages bpc "
              + "on bpu.coverage_id = bpc.id "
              + "left join benefit_coverages bc "
              + "on bpc.benefit_coverage_id = bc.id "
              + "left join benefit_plan_dependents bpd "
              + "on bpu.id = bpd.benefit_plans_users_id "
              + "left join user_dependents ud "
              + "on ud.id = bpd.user_dependents_id "
              + "where hex(bpu.benefit_plan_id) in ?1 "
              + "and bpu.confirmed is true and bpu.enrolled is true and bc.id = unhex(?2)",
      nativeQuery = true)
  List<BenefitDependentUserNamePojo> getDependentUserNameByPlanUserId(
      List<String> ids, String coverageId);

  @Query(
      value =
          "select hex(id) as benefitPlanId, "
              + "name as benefitPlanName, "
              + "start_date as deductionsBegin, "
              + "end_date as deductionsEnd, "
              + "if(start_date > current_timestamp,'Starting soon','Active') as status "
              + "from benefit_plans "
              + "where benefit_plan_type_id = unhex(?1) "
              + "and company_id = unhex(?2) "
              + "and end_date > current_timestamp",
      countQuery =
          "select count(1) from benefit_plans where benefit_plan_type_id = unhex(?1) "
              + "and company_id = unhex(?2) and end_date > current_timestamp",
      nativeQuery = true)
  Page<BenefitPlanPreviewPojo> getBenefitPlanListWithOutExpired(
      String planTypeId, String companyId, Pageable pageRequest);

  @Query(
      value =
          "select hex(id) as benefitPlanId, "
              + "name as benefitPlanName, "
              + "start_date as deductionsBegin, "
              + "end_date as deductionsEnd, "
              + "if(start_date > current_timestamp,'Starting soon',"
              + "if(current_timestamp > end_date,'Expired','Active')) as status "
              + "from benefit_plans "
              + "where benefit_plan_type_id = unhex(?1) "
              + "and company_id = unhex(?2)",
      countQuery =
          "select count(1) from benefit_plans where benefit_plan_type_id = unhex(?1) "
              + "and company_id = unhex(?2)",
      nativeQuery = true)
  Page<BenefitPlanPreviewPojo> getBenefitPlanList(
      String planTypeId, String companyId, Pageable pageRequest);
}
