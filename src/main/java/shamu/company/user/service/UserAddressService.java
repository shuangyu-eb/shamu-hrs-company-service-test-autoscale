package shamu.company.user.service;

import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.entity.UserAddress;

public interface UserAddressService {

  UserAddress findUserAddressById(Long id);

  UserAddress findUserAddressByUserId(Long id);

  UserAddress save(UserAddressDto userAddressDto);
}
