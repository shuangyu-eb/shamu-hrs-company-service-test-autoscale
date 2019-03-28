package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.UserAddress;
import com.tardisone.companyservice.repository.UserAddressRepository;
import com.tardisone.companyservice.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    UserAddressRepository repository;

    @Override
    public UserAddress findUserAddressByUserId(Long userId) {
        return repository.findUserAddressByUserId(userId);
    }

    @Override
    public void update(UserAddress userAddress) {
        repository.save(userAddress);
    }
}
