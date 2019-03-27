package com.tardisone.companyservice.service;

import com.tardisone.companyservice.entity.UserAddress;

public interface UserAddressService {
    UserAddress findUserAddressByUserId(Long userId);
}
