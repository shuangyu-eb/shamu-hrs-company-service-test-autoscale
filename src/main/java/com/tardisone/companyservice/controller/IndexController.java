package com.tardisone.companyservice.controller;

import com.tardisone.companyservice.config.annotations.RestApiController;
import com.tardisone.companyservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestApiController
public class IndexController {

    @Autowired
    UserService userService;

    @GetMapping(value = {"index", ""})
    public String index() {
        return "hello world";
    }


}
