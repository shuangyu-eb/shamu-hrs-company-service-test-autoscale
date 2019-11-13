package shamu.company.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.employee.dto.SelectFieldSizeDto;

public interface DepartmentRepository extends BaseRepository<Department, Long> {

  @Query(value =
          "select * from departments d "
                  + " where d.company_id = ?1 ",
          nativeQuery = true)
  List<Department> findAllByCompanyId(Long companyId);

  @Query(
          value = "SELECT count(1) FROM jobs_users ju"
                  + " join jobs j on ju.job_id = j.id"
                  + " WHERE j.department_id = ?1 ",
          nativeQuery = true)
  Integer getCountByDepartment(Long departmentId);
}
