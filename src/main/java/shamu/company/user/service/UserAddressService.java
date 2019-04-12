package shamu.company.user.service;

import shamu.company.user.dto.UserAddressDTO;
import shamu.company.user.entity.UserAddress;

public interface UserAddressService {
    UserAddressDTO updateUserAddress(UserAddressDTO userAddressDto);

    UserAddress findUserAddressByUserId(Long userId);


}
