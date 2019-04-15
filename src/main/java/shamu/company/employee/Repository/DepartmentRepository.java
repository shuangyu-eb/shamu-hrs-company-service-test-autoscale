package shamu.company.employee.Repository;

import shamu.company.common.BaseRepository;
import shamu.company.company.entity.Department;

public interface DepartmentRepository extends BaseRepository<Department, Long> {

    public Department findByName(String name);
}
