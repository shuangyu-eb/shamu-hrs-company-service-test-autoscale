package com.tardisone.companyservice.controller;

import com.tardisone.companyservice.entity.UserAddress;
import com.tardisone.companyservice.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company")
public class UserAddressController {
    @Autowired
    UserAddressService service;

    @GetMapping("userAddress/findByUserId/{userId}")
    public UserAddress findUserAddressByUserId(@PathVariable Long userId){
        return service.findUserAddressByUserId(userId);
    }

    @PostMapping("userAddress/update")
    public void update(@RequestBody UserAddress userAddress){
        service.update(userAddress);
    }
}
