package shamu.company.employee.service;

import java.util.List;
import shamu.company.employee.dto.GeneralObjectDto;
import shamu.company.employee.pojo.EmployeeInfomationPojo;
import shamu.company.employee.pojo.OfficePojo;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;

public interface EmployeeService {

  List<GeneralObjectDto> getJobInformation();

  User addNewUser(EmployeeInfomationPojo pojo);

  void saveUser(User user);

  void handlePersonalInformation(EmployeeInfomationPojo pojo, User user);

  void handleFullPersonalInformation(EmployeeInfomationPojo pojo, User user,
      UserPersonalInformation userPersonalInformation);

  void handleContactInformation(EmployeeInfomationPojo pojo, User user);

  void handleJobInformation(EmployeeInfomationPojo pojo, User user);

  void handelEmergencyContacts(EmployeeInfomationPojo employeePojo, User user);

  Boolean saveEmploymentType(String employmentType);

  Boolean saveDepartment(String department);

  Boolean saveOfficeLocation(OfficePojo officePojo);

}
