package com.tardisone.companyservice.service;

import com.tardisone.companyservice.dto.JobUserDTO;
import com.tardisone.companyservice.dto.NormalObjectDTO;
import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.pojo.EmployeeInfomationPojo;

import java.util.List;

public interface UserService {
    User findUserByEmail(String email);

    Boolean sendVerifyEmail(String email);

    Boolean finishUserVerification(String activationToken);

    List<JobUserDTO> findAllEmployees();

    public List<NormalObjectDTO>  getJobInformation();

    public User addNewUser(EmployeeInfomationPojo pojo);

    //注册前，处理persionnalInformation
    public void handlePersonalInformation(EmployeeInfomationPojo pojo, User user);


    public void handleContactInformation(EmployeeInfomationPojo pojo, User user);

    //处理jobInformation
    public void handleJobInformation(EmployeeInfomationPojo pojo, User user);
}
