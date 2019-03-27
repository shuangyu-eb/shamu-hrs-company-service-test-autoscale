package com.tardisone.companyservice.controller;

import com.tardisone.companyservice.entity.UserAddress;
import com.tardisone.companyservice.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/company")
public class UserAddressController {
    @Autowired
    UserAddressService service;

    @GetMapping("userAddress/findByUserId/{userId}")
    public UserAddress findUserAddressByUserId(@PathVariable Long userId){
        return service.findUserAddressByUserId(userId);
    }
}
