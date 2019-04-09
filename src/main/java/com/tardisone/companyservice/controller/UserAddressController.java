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
        UserAddress userAddress = new UserAddress();

        String cityName = userAddressDto.getCityName();
        City city = cityService.findCityByName(cityName);

        String countryName = userAddressDto.getCountryName();
        Country country = countryService.findCountryByName(countryName);

        Long userId = userAddressDto.getUserId();
        User user = userService.findById(userId);

        Long stateProvinceId = userAddressDto.getStateProvinceId();
        StateProvince stateProvince = stateProvinceService.findById(stateProvinceId);

        userAddress.setCity(city);
        userAddress.setCountry(country);
        userAddress.setPostalCode(userAddressDto.getPostalCode());
        userAddress.setStateProvince(stateProvince);
        userAddress.setUser(user);
        userAddress.setStreet1(userAddressDto.getStreet1());
        userAddress.setStreet2(userAddressDto.getStreet2());
        userAddress.setId(userAddressDto.getId());

        return service.update(userAddress);
    }
}
