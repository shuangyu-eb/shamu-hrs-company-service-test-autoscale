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

    @GetMapping("userContactInfo/{emailWork}")
    public UserContactInformation findUserContactInformationByEmailWork(@PathVariable("emailWork") String emailWork){
        return service.findUserContactInformationByEmailWork(emailWork);
    }

    @PostMapping("userContactInfo/update")
    public void update(@RequestBody UserContactInformation userContactInformation){
        service.update(userContactInformation);
    }
}
