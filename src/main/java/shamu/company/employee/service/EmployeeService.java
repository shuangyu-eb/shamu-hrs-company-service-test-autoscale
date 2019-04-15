package shamu.company.employee.service;

import shamu.company.employee.dto.GeneralObjectDTO;
import shamu.company.employee.pojo.EmployeeInfomationPojo;
import shamu.company.employee.pojo.OfficePojo;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;

import java.util.List;

public interface EmployeeService {

    List<GeneralObjectDTO> getJobInformation();

    User addNewUser(EmployeeInfomationPojo pojo);

    void saveUser(User user);

    void handlePersonalInformation(EmployeeInfomationPojo pojo, User user);

    void handleFullPersonalInformation(EmployeeInfomationPojo pojo, User user, UserPersonalInformation userPersonalInformation);

    void handleContactInformation(EmployeeInfomationPojo pojo, User user);

    void handleJobInformation(EmployeeInfomationPojo pojo, User user);

    void handelEmergencyContacts(EmployeeInfomationPojo employeePojo, User user);

    Boolean saveEmploymentType(String employmentType);

    Boolean saveDepartment(String department);

    Boolean saveOfficeLocation(OfficePojo officePojo);

}
