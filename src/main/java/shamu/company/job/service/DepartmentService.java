package shamu.company.job.service;

import java.util.List;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;

public interface DepartmentService {

  public List<Department> getDepartmentsByCompany(Company company);
}
