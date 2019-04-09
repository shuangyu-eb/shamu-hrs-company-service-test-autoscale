package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.dto.UserAddressDto;
import com.tardisone.companyservice.entity.*;
import com.tardisone.companyservice.repository.*;
import com.tardisone.companyservice.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    UserAddressRepository repository;

    @Autowired
    CityRepository cityRepository;

    @Autowired
    StateProvinceRepository stateProvinceRepository;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    UserRepository userRepository;


    @Override
    public UserAddress findUserAddressByUserId(Long userId) {
        return repository.findUserAddressByUserId(userId);
    }

    @Override
    public UserAddress update(UserAddressDto userAddressDto) {
        UserAddress userAddress = new UserAddress();

        String cityName = userAddressDto.getCityName();
        City city = cityRepository.findCityByName(cityName);

        String countryName = userAddressDto.getCountryName();
        Country country = countryRepository.findCountryByName(countryName);

        Long userId = userAddressDto.getUserId();
        User user = userRepository.getOne(userId);

        Long stateProvinceId = userAddressDto.getStateProvinceId();
        StateProvince stateProvince = stateProvinceRepository.getOne(stateProvinceId);

        userAddress.setCity(city);
        userAddress.setCountry(country);
        userAddress.setPostalCode(userAddressDto.getPostalCode());
        userAddress.setStateProvince(stateProvince);
        userAddress.setUser(user);
        userAddress.setStreet1(userAddressDto.getStreet1());
        userAddress.setStreet2(userAddressDto.getStreet2());
        userAddress.setId(userAddressDto.getId());
        return repository.save(userAddress);
    }
}
