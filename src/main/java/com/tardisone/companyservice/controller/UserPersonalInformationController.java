package com.tardisone.companyservice.controller;

import com.tardisone.companyservice.config.annotations.RestApiController;
import com.tardisone.companyservice.entity.UserPersonalInformation;
import com.tardisone.companyservice.service.UserPersonalInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestApiController
public class UserPersonalInformationController {
    @Autowired
    UserPersonalInformationService service;

    @PatchMapping("user-personal-info")
    public UserPersonalInformation update(@RequestBody UserPersonalInformation UserPersonalInformation) {
         return service.update(UserPersonalInformation);
    }

}
