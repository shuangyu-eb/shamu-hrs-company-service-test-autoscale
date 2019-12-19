package shamu.company.benefit.repository;

import java.util.List;
import java.util.Optional;
import shamu.company.benefit.entity.BenefitDependentRecord;
import shamu.company.common.repository.BaseRepository;

public interface BenefitPlanDependentRepository
    extends BaseRepository<BenefitDependentRecord, String> {

  List<BenefitDependentRecord> findByBenefitPlansUsersId(String planId);

  Optional<BenefitDependentRecord> findByBenefitPlansUsersIdAndUserDependentsId(
      String benefitPlanId, String userDependentId);
}
