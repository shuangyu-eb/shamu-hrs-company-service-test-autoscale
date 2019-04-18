package shamu.company.employee.service;

import java.util.List;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.pojo.OfficePojo;

public interface EmployeeService {

  List<SelectFieldInformationDto> getEmploymentTypes();

  List<SelectFieldInformationDto> getDepartments();

  List<SelectFieldInformationDto> getOfficeLocations();

  List<SelectFieldInformationDto> getManagers();

  EmploymentType saveEmploymentType(String employmentType);

  Department saveDepartment(String department);

  Office saveOfficeLocation(OfficePojo officePojo);
}
