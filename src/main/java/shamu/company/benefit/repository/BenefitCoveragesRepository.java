package shamu.company.benefit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import shamu.company.benefit.entity.BenefitCoverages;
import shamu.company.common.repository.BaseRepository;


public interface BenefitCoveragesRepository extends BaseRepository<BenefitCoverages, String> {

  List<BenefitCoverages> findAllByBenefitPlanIdAndBenefitPlanIdIsNull(String benefitPlanId);

  List<BenefitCoverages> findAllByBenefitPlanIdIsNull();

  List<BenefitCoverages> findAllByBenefitPlanId(String benefitPlanId);

  @Query(
      value =
          "select hex(id) from benefit_coverages "
              + "where name = ?1 and hex(benefit_plan_id) in ?2",
      nativeQuery = true)
  List<String> getCoverageIdsByNameAndPlan(String name, List<String> benefitPlanIds);
}
