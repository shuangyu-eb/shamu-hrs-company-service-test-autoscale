package com.tardisone.companyservice.controller;

import com.tardisone.companyservice.config.annotations.RestApiController;
import com.tardisone.companyservice.entity.UserContactInformation;
import com.tardisone.companyservice.service.UserContactInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestApiController
public class UserContactInformationController {

    @Autowired
    UserContactInformationService service;

    @PatchMapping("user-contact-info")
    public UserContactInformation update(@RequestBody UserContactInformation userContactInformation){
        return service.update(userContactInformation);
    }
}
