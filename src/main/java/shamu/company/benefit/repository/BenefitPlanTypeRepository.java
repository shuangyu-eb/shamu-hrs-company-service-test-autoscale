package shamu.company.benefit.repository;

import java.util.List;
import shamu.company.benefit.entity.BenefitPlanType;
import shamu.company.common.repository.BaseRepository;

public interface BenefitPlanTypeRepository extends BaseRepository<BenefitPlanType, String> {

  List<BenefitPlanType> findAll();
}
