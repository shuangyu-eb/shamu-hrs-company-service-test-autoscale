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
    public void update(@RequestBody UserAddress userAddress){

        UserAddress entity = new UserAddress();
        entity.setId(userAddress.getId());
        entity.setUserId(userAddress.getUserId());
        entity.setStreet_1(userAddress.getStreet_1());
        entity.setStreet_2(userAddress.getStreet_2());

        String cityName = userAddress.getCity().getName();
        City city = cityService.findCityByName(cityName);
        entity.setCity(city);

        String countryName = userAddress.getCountry().getName();
        Country country = countryService.findCountryByName(countryName);
        entity.setCountry(country);


        entity.setStateProvinceId(userAddress.getStateProvinceId());
        entity.setPostalCode(userAddress.getPostalCode());
        service.update(entity);
    }
}