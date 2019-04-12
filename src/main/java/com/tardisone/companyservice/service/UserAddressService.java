package com.tardisone.companyservice.service;

import com.tardisone.companyservice.dto.PersonalInformationDTO;
import com.tardisone.companyservice.dto.UserAddressDTO;

public interface UserAddressService {
    UserAddressDTO updateUserAddress(UserAddressDTO userAddressDto);

    PersonalInformationDTO getPersonalInformation(Long userId);
}
