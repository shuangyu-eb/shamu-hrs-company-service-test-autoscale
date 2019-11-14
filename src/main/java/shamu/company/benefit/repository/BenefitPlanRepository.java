package shamu.company.benefit.repository;

import java.util.List;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.common.repository.BaseRepository;

public interface BenefitPlanRepository extends BaseRepository<BenefitPlan, String> {

  List<BenefitPlan> findBenefitPlanByCompanyId(String companyId);
}
