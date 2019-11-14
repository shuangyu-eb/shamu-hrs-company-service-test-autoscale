package shamu.company.benefit.repository;

import java.util.List;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanUser;
import shamu.company.common.repository.BaseRepository;

public interface BenefitPlanUserRepository extends BaseRepository<BenefitPlanUser, String> {
  List<BenefitPlanUser> findAllByBenefitPlan(BenefitPlan benefitPlan);
}
