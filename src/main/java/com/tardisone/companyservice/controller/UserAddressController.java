package com.tardisone.companyservice.controller;

import com.tardisone.companyservice.entity.City;
import com.tardisone.companyservice.entity.Country;
import com.tardisone.companyservice.entity.UserAddress;
import com.tardisone.companyservice.service.CityService;
import com.tardisone.companyservice.service.CountryService;
import com.tardisone.companyservice.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company")
public class UserAddressController {
    @Autowired
    UserAddressService service;

    @Autowired
    CityService cityService;

    @Autowired
    CountryService countryService;

    @GetMapping("user-address/{userId}")
    public UserAddress findUserAddressByUserId(@PathVariable Long userId){
        return service.findUserAddressByUserId(userId);
    }

    @PostMapping("user-address")
    public UserAddress update(@RequestBody UserAddress userAddress){
        return service.update(userAddress);
    }
}
