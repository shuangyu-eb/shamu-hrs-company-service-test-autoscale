package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.dto.PersonalInformationDTO;
import com.tardisone.companyservice.dto.UserAddressDTO;
import com.tardisone.companyservice.entity.*;
import com.tardisone.companyservice.repository.*;
import com.tardisone.companyservice.service.CountryService;
import com.tardisone.companyservice.service.StateProvinceService;
import com.tardisone.companyservice.service.UserAddressService;
import com.tardisone.companyservice.service.UserService;
import com.tardisone.companyservice.utils.PersonalInformationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public PersonalInformationDTO getPersonalInformation(Long userId) {
        UserAddress entity = repository.findUserAddressByUserId(userId);
        PersonalInformationDTO personalInformationDTO = PersonalInformationUtil.convert(entity);
        return personalInformationDTO;
    }

    @Override
    public UserAddressDTO updateUserAddress(UserAddressDTO userAddressDTO) {
        String city = userAddressDTO.getCity();

        String countryName = userAddressDTO.getCountryName();
        Country country = countryService.getCountry(countryName);

        Long userId = userAddressDTO.getUserId();
        User user = userService.getUser(userId);


        Long stateProvinceId = userAddressDTO.getStateProvinceId();
        StateProvince stateProvince = stateProvinceService.getStateProvince(stateProvinceId);


        String postalCode = userAddressDTO.getPostalCode();
        String street1 = userAddressDTO.getStreet1();
        String street2 = userAddressDTO.getStreet2();
        Long id = userAddressDTO.getId();

        UserAddress userAddress = new UserAddress(user,street1,street2,city,stateProvince,country,postalCode);
        userAddress.setId(id);

        UserAddress userAddressUpdated = repository.save(userAddress);

        UserAddressDTO userAddressDTOUpdated = PersonalInformationUtil.convertUserAddressEntityToDTO(userAddressUpdated);

        return userAddressDTOUpdated;
    }
}
