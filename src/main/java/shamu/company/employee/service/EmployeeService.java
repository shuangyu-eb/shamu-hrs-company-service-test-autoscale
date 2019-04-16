package shamu.company.employee.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import shamu.company.employee.dto.GeneralObjectDto;
import shamu.company.employee.pojo.OfficePojo;

public interface EmployeeService {

  List<GeneralObjectDto> getJobInformation();

  Long saveEmploymentType(String employmentType);

  Long saveDepartment(String department);

  Long saveOfficeLocation(OfficePojo officePojo);

}
