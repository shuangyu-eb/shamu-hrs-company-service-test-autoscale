package shamu.company.benefit.repository;

import java.util.List;
import shamu.company.benefit.entity.BenefitCoverages;
import shamu.company.common.repository.BaseRepository;


public interface BenefitCoveragesRepository extends BaseRepository<BenefitCoverages, String> {

  List<BenefitCoverages> findAllByBenefitPlanIdAndBenefitPlanIdIsNull(String benefitPlanId);

  List<BenefitCoverages> findAllByBenefitPlanIdIsNull();

  List<BenefitCoverages> findAllByBenefitPlanId(String benefitPlanId);
}
