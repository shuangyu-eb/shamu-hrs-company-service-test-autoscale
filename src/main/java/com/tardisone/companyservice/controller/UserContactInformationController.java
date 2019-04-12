package com.tardisone.companyservice.controller;

import com.tardisone.companyservice.config.annotations.RestApiController;
import com.tardisone.companyservice.dto.UserContactInformationDTO;
import com.tardisone.companyservice.entity.UserContactInformation;
import com.tardisone.companyservice.service.UserContactInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestApiController
public class UserContactInformationController {

    @Autowired
    UserContactInformationService contactInformationService;

    @PatchMapping("users/user-contact-info")
    public UserContactInformationDTO update(@RequestBody UserContactInformation userContactInformation){
        return contactInformationService.update(userContactInformation);
    }
}
