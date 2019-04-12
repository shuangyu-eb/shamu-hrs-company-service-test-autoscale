package shamu.company.user.controller;

import shamu.company.common.config.annotations.RestApiController;
import shamu.company.user.dto.UserAddressDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import shamu.company.user.service.UserAddressService;

@RestApiController
public class UserAddressController {
    @Autowired
    UserAddressService userAddressService;

    @PatchMapping("user-address")
    public UserAddressDTO updateUserAddress(@RequestBody UserAddressDTO userAddressDto){
        return userAddressService.updateUserAddress(userAddressDto);
    }
}
