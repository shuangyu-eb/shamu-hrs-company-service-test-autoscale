package shamu.company.job.service;

import java.util.List;
import shamu.company.company.entity.Department;

public interface DepartmentService {

  public List<Department> getAllDepartments(Long companyId);
}
