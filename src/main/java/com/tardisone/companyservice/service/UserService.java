package com.tardisone.companyservice.service;

import com.tardisone.companyservice.dto.NormalObjectDto;
import com.tardisone.companyservice.model.User;
import com.tardisone.companyservice.pojo.EmployeeInfomationPojo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface UserService {

    public List<NormalObjectDto>  getJobInformation();

    public User addNewUser(EmployeeInfomationPojo pojo);

    //注册前，处理persionnalInformation
    public void handlePersonalInformation(EmployeeInfomationPojo pojo, User user);


    public void handleContactInformation(EmployeeInfomationPojo pojo, User user);

    //处理jobInformation
    public void handleJobInformation(EmployeeInfomationPojo pojo, User user);

}
