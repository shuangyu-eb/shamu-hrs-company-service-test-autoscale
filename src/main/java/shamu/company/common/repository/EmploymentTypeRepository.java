package shamu.company.common.repository;

import org.springframework.data.jpa.repository.Query;
import shamu.company.employee.entity.EmploymentType;

public interface EmploymentTypeRepository extends BaseRepository<EmploymentType, String> {

  @Query(
      value = "SELECT count(1) FROM jobs_users ju" + " WHERE ju.employment_type_id = unhex(?1) ",
      nativeQuery = true)
  Integer findCountByType(String typeId);
}
