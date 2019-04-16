package shamu.company.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.service.UserAddressService;

@RestApiController
public class UserAddressRestController {
  @Autowired UserAddressService userAddressService;

  @PatchMapping("user-address")
  public UserAddressDto updateUserAddress(@RequestBody UserAddressDto userAddressDto) {
    return userAddressService.updateUserAddress(userAddressDto);
  }
}
