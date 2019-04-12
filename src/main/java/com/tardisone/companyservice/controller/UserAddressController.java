package com.tardisone.companyservice.controller;

import com.tardisone.companyservice.config.annotations.RestApiController;
import com.tardisone.companyservice.dto.UserAddressDTO;
import com.tardisone.companyservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestApiController
public class UserAddressController {
    @Autowired
    UserAddressService userAddressService;

    @PatchMapping("users/user-address")
    public UserAddressDTO updateUserAddress(@RequestBody UserAddressDTO userAddressDto){
        return userAddressService.updateUserAddress(userAddressDto);
    }
}
