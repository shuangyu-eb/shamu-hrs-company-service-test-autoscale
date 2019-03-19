package com.tardisone.companyservice.service;

import com.tardisone.companyservice.exception.CheckFaildException;
import com.tardisone.companyservice.model.User;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

public interface UserService {


    public User addNewUser(HttpServletRequest request);

    //注册前，处理persionnalInformation
    public void handlePersonalInformation(HttpServletRequest request, User user);


    public void handleContactInformation(HttpServletRequest request, User user);

    //处理jobInformation
    public void handleJobInformation(HttpServletRequest request, User user);

}
