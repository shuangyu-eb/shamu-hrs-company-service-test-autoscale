package com.tardisone.companyservice.controller;

import com.tardisone.companyservice.entity.UserPersonalInformation;
import com.tardisone.companyservice.service.UserPersonalInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company")
public class UserPersonalInformationController {
    @Autowired
    UserPersonalInformationService service;

    @GetMapping("userPersonalInfo/{emailWork}")
    public UserPersonalInformation findUserPersonalInformationByEmailWork(@PathVariable(name = "emailWork") String emailWork){
        return service.findUserPersonalInformationByEmailWork(emailWork);
    }

    @GetMapping("userPersonalInfo/findById/{id}")
    public UserPersonalInformation findUserPersonalInformationById(@PathVariable Long id){
        return service.findUserPersonalInformationById(id);
    }

    @PostMapping("userPersonalInfo/update")
    public void update(@RequestBody UserPersonalInformation UserPersonalInformation) {
         service.update(UserPersonalInformation);
    }

}
