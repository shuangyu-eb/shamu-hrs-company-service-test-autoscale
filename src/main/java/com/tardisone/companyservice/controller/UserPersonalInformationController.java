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

    @GetMapping("user-personal-info/{id}")
    public UserPersonalInformation findUserPersonalInformationById(@PathVariable Long id){
        return service.findUserPersonalInformationById(id);
    }

    @GetMapping("user-personal-info/findByFirstName/{firstName}")
    public UserPersonalInformation findUserPersonalInformationByFirstName(@PathVariable String firstName){
        return service.findUserPersonalInformationByFirstName(firstName);
    }

    @PostMapping("user-personal-info")
    public void update(@RequestBody UserPersonalInformation UserPersonalInformation) {
         service.update(UserPersonalInformation);
    }

}
