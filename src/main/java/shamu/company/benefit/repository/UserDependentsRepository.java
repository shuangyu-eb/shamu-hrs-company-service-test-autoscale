package shamu.company.benefit.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.common.repository.BaseRepository;

public interface UserDependentsRepository extends BaseRepository
    <BenefitPlanDependent, String> {

  @Query(
      value =
          "SELECT * FROM user_dependents " + "WHERE employee_id = unhex(?1) order by last_name ASC",
      nativeQuery = true)
  List<BenefitPlanDependent> findByUserId(String id);

  Long countByEmployeeId(String id);
}
