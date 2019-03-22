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
import java.util.Map;

import com.tardisone.companyservice.util.Constants;

import javax.servlet.http.HttpServletRequest;

@RestController
public class IndexController {

    @Autowired
    UserService userService;

    @GetMapping(value = {"/index", "/"})
    public String index() {
        return "hello world";
    }




}
