package shamu.company.user.service;

import shamu.company.user.entity.UserAddress;

public interface UserAddressService {

  UserAddress updateUserAddress(UserAddress userAddress);

  UserAddress findUserAddressById(Long id);

  UserAddress findUserAddressByUserId(Long id);

  UserAddress save(UserAddress userAddress);
}
