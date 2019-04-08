package com.tardisone.companyservice.controller;

import com.tardisone.companyservice.entity.UserContactInformation;
import com.tardisone.companyservice.service.UserContactInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company")
public class UserContactInformationController {

    @Autowired
    UserContactInformationService service;


    @PatchMapping("user-contact-info")
    public UserContactInformation update(@RequestBody UserContactInformation userContactInformation){
        return service.update(userContactInformation);
    }

    @GetMapping("user-contact-info/{id}")
    public UserContactInformation findUserContactInformationById(@PathVariable("id") Long id){
        return service.findUserContactInformationById(id);
    }
}
