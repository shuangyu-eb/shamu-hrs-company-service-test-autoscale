package shamu.company.job.service;

import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.job.pojo.JobInformationPojo;
import shamu.company.job.pojo.OfficeAddressPojo;
import shamu.company.user.entity.User;

public interface JobService {

  void saveEmploymentType(EmploymentType employmentType, Company company);

  void saveDepartment(Department department, User user);

  void saveOfficeAddress(OfficeAddressPojo addressPojo);

  void updateJobInfo(JobInformationPojo jobInfoEditPojo, User user);
}
