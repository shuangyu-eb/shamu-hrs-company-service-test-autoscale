package shamu.company.benefit.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.common.repository.BaseRepository;

public interface BenefitPlanDependentRepository extends BaseRepository
    <BenefitPlanDependent, Long> {

  @Query(
      value = "SELECT * FROM benefit_plan_dependents "
          + "WHERE deleted_at IS NULL AND employee_id = ?1",
      nativeQuery = true)
  List<BenefitPlanDependent> findByUserId(Long id);

  Optional<BenefitPlanDependent> findById(Long dependentId);
}
