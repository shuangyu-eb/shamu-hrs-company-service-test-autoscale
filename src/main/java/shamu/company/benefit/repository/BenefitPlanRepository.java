package shamu.company.benefit.repository;

import java.util.List;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.common.repository.BaseRepository;
import shamu.company.company.entity.Company;

public interface BenefitPlanRepository extends BaseRepository<BenefitPlan, Long> {

  List<BenefitPlan> findBenefitPlanByCompany(Company company);
}
