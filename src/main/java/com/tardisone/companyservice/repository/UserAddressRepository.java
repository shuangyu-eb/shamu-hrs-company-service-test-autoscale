package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.UserAddress;

public interface UserAddressRepository extends BaseRepository<UserAddress,Long>{
    UserAddress findUserAddressByUserId(Long id);
}
