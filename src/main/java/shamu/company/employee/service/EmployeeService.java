package shamu.company.employee.service;

import java.util.List;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.user.entity.User;

public interface EmployeeService {

  List<User> findEmployersAndEmployeesByDepartmentIdAndCompanyId(Long departmentId, Long companyId);

  void addEmployee(EmployeeDto employee, User currentUser);
}
