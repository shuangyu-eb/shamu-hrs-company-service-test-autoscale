package shamu.company.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;

@RestApiController
public class UserAddressRestController extends BaseRestController {

  private final UserAddressService userAddressService;

  private final UserService userService;

  @Autowired
  public UserAddressRestController(UserAddressService userAddressService, UserService userService) {
    this.userAddressService = userAddressService;
    this.userService = userService;
  }

  @PatchMapping("user-addresses/{id}")
  @PreAuthorize(
      "hasPermission(#id,'USER_ADDRESS', 'EDIT_USER')"
          + " or hasPermission(#id,'USER_ADDRESS', 'EDIT_SELF')")
  public UserAddressDto updateUserAddress(
      @PathVariable @HashidsFormat Long id, @RequestBody UserAddressDto userAddressDto) {
    UserAddress origin = userAddressService.findUserAddressById(id);
    UserAddress userAddress = userAddressDto.getUserAddress(origin);
    UserAddress userAddressUpdated = userAddressService.updateUserAddress(userAddress);
    return new UserAddressDto(userAddressUpdated);
  }

  @GetMapping("users/{id}/user-address")
  @PreAuthorize("hasPermission(#id, 'USER', 'VIEW_USER_ADDRESS')")
  public UserAddressDto getUserAddress(@PathVariable @HashidsFormat Long id) {
    User user = this.getUser();
    User targetUser = userService.findUserById(id);
    User manager = targetUser.getManagerUser();
    UserAddress userAddress = userAddressService.findUserAddressByUserId(id);

    if (user.getId().equals(id)
        || user.getRole() == Role.ADMIN
        || (manager != null && manager.getId().equals(user.getId()))) {
      return new UserAddressDto(userAddress);
    }

    return null;
  }
}
