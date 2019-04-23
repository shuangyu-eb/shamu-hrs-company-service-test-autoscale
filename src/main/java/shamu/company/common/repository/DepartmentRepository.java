package shamu.company.common.repository;

import java.util.List;
import shamu.company.company.entity.Department;

public interface DepartmentRepository extends BaseRepository<Department, Long> {

  Department findByName(String name);

  List<Department> findAllByCompanyId(Long companyId);
}
