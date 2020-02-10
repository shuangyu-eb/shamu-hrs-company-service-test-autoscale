package shamu.company.benefit.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.benefit.dto.BenefitPlanTypeDto;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.common.repository.BaseRepository;

public interface BenefitPlanRepository extends BaseRepository<BenefitPlan, String> {

  List<BenefitPlan> findBenefitPlanByCompanyId(String companyId);

  List<BenefitPlan> findBenefitPlanByIdAndCompanyId(String benefitPlanId, String companyId);

  List<BenefitPlan> findByBenefitPlanTypeIdAndCompanyId(String benefitPlanTypeId, String companyId);

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
}
