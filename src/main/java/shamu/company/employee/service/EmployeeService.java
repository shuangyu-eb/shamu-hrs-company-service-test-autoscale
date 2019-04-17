package shamu.company.employee.service;

import java.util.List;
import shamu.company.employee.dto.SelectFieldInfomationDto;
import shamu.company.employee.pojo.OfficePojo;

public interface EmployeeService {

  List<SelectFieldInfomationDto> getEmploymentTypes();

  List<SelectFieldInfomationDto> getDepartments();

  List<SelectFieldInfomationDto> getOfficeLocations();

  List<SelectFieldInfomationDto> getManagers();

  Long saveEmploymentType(String employmentType);

  Long saveDepartment(String department);

  Long saveOfficeLocation(OfficePojo officePojo);

}
