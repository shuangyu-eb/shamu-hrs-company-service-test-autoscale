package shamu.company.employee.service;

import java.util.List;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.employee.pojo.OfficePojo;

public interface EmployeeService {

  List<SelectFieldInformationDto> getEmploymentTypes();

  List<SelectFieldInformationDto> getDepartments();

  List<SelectFieldInformationDto> getOfficeLocations();

  List<SelectFieldInformationDto> getManagers();

  Long saveEmploymentType(String employmentType);

  Long saveDepartment(String department);

  Long saveOfficeLocation(OfficePojo officePojo);

}
