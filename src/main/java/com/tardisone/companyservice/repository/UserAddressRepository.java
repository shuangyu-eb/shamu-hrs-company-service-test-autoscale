package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.entity.UserAddress;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAddressRepository extends BaseRepository<UserAddress, Long> {

    List<UserAddress> findAllByUserIn(List<User> users);

    UserAddress findUserAddressByUserId(Long userId);

    UserAddress findUserAddressById(Long id);

    List<UserAddress> findAllById(Long id);

}
