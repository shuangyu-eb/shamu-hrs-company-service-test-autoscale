package shamu.company.employee.service;

import java.util.List;
import shamu.company.email.Email;
import shamu.company.employee.dto.EmailResendDto;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.user.entity.User;

public interface EmployeeService {

  List<User> findEmployersAndEmployeesByDepartmentIdAndCompanyId(Long departmentId, Long companyId);

  List<User> findDirectReportsEmployersAndEmployeesByDepartmentIdAndCompanyId(
          Long departmentId, Long companyId, Long userId);

  void addEmployee(EmployeeDto employee, User currentUser);

  void updateEmployee(EmployeeDto employeeDto, User employee);

  void resendEmail(EmailResendDto emailResendDto);

  Email getWelcomeEmail(String email);
}
