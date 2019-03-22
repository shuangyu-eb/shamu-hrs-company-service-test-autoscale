package com.tardisone.companyservice.controller;

import com.tardisone.companyservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @Autowired
    UserService userService;

    @GetMapping(value = {"/index", "/"})
    public String index() {
        return "hello world";
    }




}
