package shamu.company.employee.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import shamu.company.employee.dto.GeneralObjectDto;
import shamu.company.employee.pojo.OfficePojo;

public interface EmployeeService {

  List<GeneralObjectDto> getEmploymentTypes();

  List<GeneralObjectDto> getDepartments();

  List<GeneralObjectDto> getOfficeLocations();

  List<GeneralObjectDto> getManagers();

  Long saveEmploymentType(String employmentType);

  Long saveDepartment(String department);

  Long saveOfficeLocation(OfficePojo officePojo);

}
