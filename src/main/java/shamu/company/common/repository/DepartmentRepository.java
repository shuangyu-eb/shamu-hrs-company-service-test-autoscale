package shamu.company.common.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import shamu.company.company.entity.Department;

public interface DepartmentRepository extends BaseRepository<Department, String> {

  @Query(
      value =
          "SELECT count(1) FROM jobs_users ju"
              + " join jobs j on ju.job_id = j.id"
              + " WHERE department_id = unhex(?1) ",
      nativeQuery = true)
  Integer findCountByDepartment(String departmentId);

  @Query(value = "SELECT * FROM departments d WHERE binary d.name = ?1 ", nativeQuery = true)
  List<Department> findByName(String name);
}
