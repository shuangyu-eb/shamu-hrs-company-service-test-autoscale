package shamu.company.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.service.UserAddressService;

@RestApiController
public class UserAddressRestController extends BaseRestController {

  private final UserAddressService userAddressService;

  @Autowired
  public UserAddressRestController(UserAddressService userAddressService) {
    this.userAddressService = userAddressService;
  }

  @PatchMapping("user-addresses")
  public UserAddressDto updateUserAddress(@RequestBody UserAddressDto userAddressDto) {
    UserAddress userAddress = userAddressDto.getUserAddress();
    UserAddress userAddressUpdated = userAddressService.updateUserAddress(userAddress);
    return new UserAddressDto(userAddressUpdated);
  }
}
