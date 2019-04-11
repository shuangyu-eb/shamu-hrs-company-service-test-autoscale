package com.tardisone.companyservice.controller;

import com.tardisone.companyservice.config.annotations.RestApiController;
import com.tardisone.companyservice.dto.UserAddressDto;
import com.tardisone.companyservice.entity.*;
import com.tardisone.companyservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestApiController
public class UserAddressController {
    @Autowired
    UserAddressService userAddressService;

    @GetMapping("user-address/{userId}")
    public UserAddress getUserAddress(@PathVariable Long userId){
        UserAddress userAddress = userAddressService.getUserAddress(userId);
        return userAddress;
    }

    @PatchMapping("user-address")
    public UserAddress update(@RequestBody UserAddressDto userAddressDto){
        return userAddressService.update(userAddressDto);
    }
}
