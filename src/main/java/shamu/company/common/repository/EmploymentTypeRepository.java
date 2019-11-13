package shamu.company.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import shamu.company.employee.entity.EmploymentType;

public interface EmploymentTypeRepository extends BaseRepository<EmploymentType, Long> {

  List<EmploymentType> findAllByCompanyId(Long companyId);

  @Query(
          value = "SELECT count(1) FROM user_employment_status_types uest"
                  + " WHERE uest.id = ?1 ",
          nativeQuery = true)
  Integer getCountByType(Long typeId);
}
