package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.entity.UserAddress;

import java.util.List;

public interface UserAddressRepository extends BaseRepository<UserAddress, Long> {

    List<UserAddress> findAllByUserIn(List<User> users);

    UserAddress findUserAddressByUserId(Long id);
}
