package com.tardisone.companyservice.controller;

import com.google.gson.JsonArray;
import com.tardisone.companyservice.model.User;
import com.tardisone.companyservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import com.tardisone.companyservice.util.Constants;

@RestController
public class IndexController {

    @Autowired
    UserService userService;

    @GetMapping(value = {"/index", "/"})
    public String index() {
        return "hello world";
    }

    @GetMapping(value = {"/getUserInformation"})
    @Transactional
    public Map<String, String> getUserInformation(HttpServletRequest request){



        return null;

    }

    @GetMapping(value = "/handleUserInformation")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public boolean handleUserInformation(HttpServletRequest request){
        boolean checkFlag = true;
        try{
            User user = userService.addNewUser(request);
            userService.handlePersonalInformation(request, user);
            userService.handleContactInformation(request, user);
            userService.handleJobInformation(request, user);
        }catch (Exception e){
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            checkFlag = false;
        }

        return checkFlag;
    }


}
