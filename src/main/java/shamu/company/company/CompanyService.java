package shamu.company.company;

import java.util.List;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.job.entity.Job;

public interface CompanyService {

  Boolean existsByName(String companyName);

  List<Department> getDepartmentsByCompany(Company company);

  Department saveDepartmentsByCompany(String name, Company company);

  List<Job> getJobsByDepartmentId(Long id);

  Job saveJobsByDepartmentId(Long departmentId, String name);

  List<Office> getOfficesByCompany(Company company);

  Office saveOffice(Office office);

  List<EmploymentType> getEmploymentTypesByCompany(Company company);

  EmploymentType saveEmploymentType(String employmentTypeName, Company company);
}
