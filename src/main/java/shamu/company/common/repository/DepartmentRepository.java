package shamu.company.common.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.company.entity.Department;

public interface DepartmentRepository extends BaseRepository<Department, String> {

  @Query(
      value = "select * from departments d " + " where d.company_id = unhex(?1) ",
      nativeQuery = true)
  List<Department> findAllByCompanyId(String companyId);

  @Query(
      value =
          "SELECT count(1) FROM jobs_users ju"
              + " join jobs j on ju.job_id = j.id"
              + " WHERE j.department_id = unhex(?1) ",
      nativeQuery = true)
  Integer findCountByDepartment(String departmentId);
}
