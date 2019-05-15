package shamu.company.common.repository;

import java.util.List;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;

public interface DepartmentRepository extends BaseRepository<Department, Long> {

  List<Department> findAllByCompany(Company company);
}
