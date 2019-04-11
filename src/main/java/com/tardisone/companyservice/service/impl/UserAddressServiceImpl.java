package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.dto.UserAddressDto;
import com.tardisone.companyservice.entity.*;
import com.tardisone.companyservice.repository.*;
import com.tardisone.companyservice.service.CountryService;
import com.tardisone.companyservice.service.StateProvinceService;
import com.tardisone.companyservice.service.UserAddressService;
import com.tardisone.companyservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    UserAddressRepository repository;

    @Autowired
    StateProvinceService stateProvinceService;

    @Autowired
    CountryService countryService;

    @Autowired
    UserService userService;


    @Override
    public UserAddress getUserAddress(Long userId) {
        return repository.findUserAddressByUserId(userId);
    }

    @Override
    public UserAddress updateUserAddress(UserAddressDto userAddressDto) {
        String city = userAddressDto.getCity();

        String countryName = userAddressDto.getCountryName();
        Country country = countryService.getCountry(countryName);

        Long userId = userAddressDto.getUserId();
        User user = userService.getUser(userId);


        Long stateProvinceId = userAddressDto.getStateProvinceId();
        StateProvince stateProvince = stateProvinceService.getStateProvince(stateProvinceId);


        String postalCode = userAddressDto.getPostalCode();

        String street1 = userAddressDto.getStreet1();

        String street2 = userAddressDto.getStreet2();

        UserAddress userAddress = new UserAddress(user,street1,street2,city,stateProvince,country,postalCode);

        return repository.save(userAddress);
    }
}
