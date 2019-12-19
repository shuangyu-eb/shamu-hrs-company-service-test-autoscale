package shamu.company.benefit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import shamu.company.benefit.dto.BenefitPlanTypeDto;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.common.repository.BaseRepository;

public interface BenefitPlanRepository extends BaseRepository<BenefitPlan, String> {

  List<BenefitPlan> findBenefitPlanByCompanyId(String companyId);

  List<BenefitPlan> findByBenefitPlanTypeIdAndCompanyId(String benefitPlanTypeId, String companyId);

  @Query("select new shamu.company.benefit.dto.BenefitPlanTypeDto("
      + "bp.benefitPlanType.id, bp.benefitPlanType.name, count(bp.benefitPlanType) )"
      + " from BenefitPlan bp where bp.company.id = ?1"
      + " group by bp.benefitPlanType"
      + " order by bp.benefitPlanType")
  List<BenefitPlanTypeDto> findPlanTypeAndNumByCompanyIdOrderTypeId(String companyId);

  BenefitPlan findBenefitPlanById(String planId);
}
