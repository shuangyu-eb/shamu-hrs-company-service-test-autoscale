package shamu.company.benefit.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.common.repository.BaseRepository;

public interface BenefitPlanDependentRepository extends BaseRepository
    <BenefitPlanDependent, String> {

  @Query(
      value = "SELECT * FROM benefit_plan_dependents "
          + "WHERE employee_id = unhex(?1)",
      nativeQuery = true)
  List<BenefitPlanDependent> findByUserId(String id);
}
