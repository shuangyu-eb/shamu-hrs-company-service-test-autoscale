package shamu.company.company;

import java.util.List;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.job.entity.Job;

public interface CompanyService {

  Boolean existsByName(String companyName);

  List<Department> getDepartmentsByCompanyId(Long companyId);

  Department getDepartmentsById(Long id);

  Department saveDepartmentsByCompany(String name, Long companyId);

  List<Job> getJobsByDepartmentId(Long id);

  Job saveJobsByDepartmentId(Long departmentId, String name);

  List<Office> getOfficesByCompany(Long companyId);

  Office saveOffice(Office office);

  List<EmploymentType> getEmploymentTypesByCompanyId(Long companyId);

  EmploymentType saveEmploymentType(String employmentTypeName, Long companyId);

  Company findById(Long companyId);
}
