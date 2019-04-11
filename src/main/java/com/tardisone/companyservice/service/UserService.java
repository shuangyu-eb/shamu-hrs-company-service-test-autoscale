package com.tardisone.companyservice.service;

import com.tardisone.companyservice.dto.JobUserDTO;
import com.tardisone.companyservice.dto.NormalObjectDTO;
import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.entity.UserPersonalInformation;
import com.tardisone.companyservice.pojo.EmployeeInfomationPojo;
import com.tardisone.companyservice.pojo.OfficePojo;

import java.util.List;

public interface UserService {
    User findUserByEmail(String email);

    Boolean sendVerifyEmail(String email);

    Boolean finishUserVerification(String activationToken);

    List<JobUserDTO> findAllEmployees();

    Boolean existsByEmailWork(String email);

    List<NormalObjectDTO>  getJobInformation();

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
