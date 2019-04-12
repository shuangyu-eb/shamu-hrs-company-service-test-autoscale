package com.tardisone.companyservice.controller;

import com.tardisone.companyservice.config.annotations.RestApiController;
import com.tardisone.companyservice.dto.PersonalInformationDTO;
import com.tardisone.companyservice.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestApiController
public class PersonalInformationController {
    @Autowired
    UserAddressService userAddressService;

    @GetMapping("users/personal-information/{userId}")
    public PersonalInformationDTO getPersonalInformation(@PathVariable Long userId){
        PersonalInformationDTO personalInformationDTO = userAddressService.getPersonalInformation(userId);
        return personalInformationDTO;
    }
}
