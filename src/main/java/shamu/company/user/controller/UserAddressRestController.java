package shamu.company.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.mapper.UserAddressMapper;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;

@RestApiController
public class UserAddressRestController extends BaseRestController {

  private final UserAddressService userAddressService;

  private final UserService userService;

  private final UserAddressMapper userAddressMapper;

  @Autowired
  public UserAddressRestController(final UserAddressService userAddressService,
      final UserService userService,
      final UserAddressMapper userAddressMapper) {
    this.userAddressService = userAddressService;
    this.userService = userService;
    this.userAddressMapper = userAddressMapper;
  }

  @PatchMapping("users/{userId}/user-address")
  @PreAuthorize(
      "hasPermission(#userAddressDto.userId,'USER', 'EDIT_USER')"
          + " or hasPermission(#userAddressDto.userId,'USER', 'EDIT_SELF')")
  public UserAddressDto saveUserAddress(@RequestBody final UserAddressDto userAddressDto) {
    return userAddressMapper.convertToUserAddressDto(userAddressService.save(userAddressDto));
  }

  @GetMapping("users/{id}/user-address")
  @PreAuthorize(
      "hasPermission(#id, 'USER', 'VIEW_USER_ADDRESS')"
          + "or hasPermission(#id, 'USER', 'VIEW_SELF')")
  public UserAddressDto getUserAddress(@PathVariable final String id) {
    final User targetUser = userService.findById(id);
    final User manager = targetUser.getManagerUser();
    final UserAddress userAddress = userAddressService.findUserAddressByUserId(id);

    final User currentUser = userService.findByUserId(getUserId());
    final Role userRole = currentUser.getRole();
    if (userAddress != null && (getAuthUser().getId().equals(id)
        || userRole == Role.ADMIN
        || (manager != null && manager.getId().equals(getAuthUser().getId())))) {
      return userAddressMapper.convertToUserAddressDto(userAddress);
    }

    return null;
  }
}
