package com.tardisone.companyservice.service;

import com.tardisone.companyservice.dto.UserAddressDto;
import com.tardisone.companyservice.entity.UserAddress;

public interface UserAddressService {
    UserAddress findUserAddressByUserId(Long userId);
    UserAddress update(UserAddressDto userAddressDto);
}
