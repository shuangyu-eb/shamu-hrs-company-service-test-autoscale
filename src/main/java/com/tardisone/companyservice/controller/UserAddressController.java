package com.tardisone.companyservice.controller;

import com.tardisone.companyservice.dto.UserAddressDto;
import com.tardisone.companyservice.entity.*;
import com.tardisone.companyservice.service.*;
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

    @Autowired
    UserService userService;

    @Autowired
    StateProvinceService stateProvinceService;

    @GetMapping("user-address/{userId}")
    public UserAddress findUserAddressByUserId(@PathVariable Long userId){
        return service.findUserAddressByUserId(userId);
    }

    @PatchMapping("user-address")
    public UserAddress update(@RequestBody UserAddressDto userAddressDto){
        return service.update(userAddressDto);
    }
}
