package shamu.company.employee.service;

import java.util.List;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.pojo.OfficePojo;
import shamu.company.user.entity.User;

public interface EmployeeService {

  List<SelectFieldInformationDto> getEmploymentTypes();

  List<SelectFieldInformationDto> getDepartments();

  List<SelectFieldInformationDto> getOfficeLocations();

  List<SelectFieldInformationDto> getManagers();

  EmploymentType saveEmploymentType(String employmentType);

  Department saveDepartment(String department);

  Office saveOfficeLocation(OfficePojo officePojo);

  void addEmployee(EmployeeDto employee, User currentUser);

  String getEmployeeNumber(String companyName, Integer employeeNumber);
}
