package shamu.company.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.mapper.UserAddressMapper;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;
import shamu.company.utils.Auth0Util;

@RestApiController
public class UserAddressRestController extends BaseRestController {

  private final UserAddressService userAddressService;

  private final UserService userService;

  private final UserAddressMapper userAddressMapper;

  private final Auth0Util auth0Util;

  @Autowired
  public UserAddressRestController(final UserAddressService userAddressService,
      final UserService userService,
      final UserAddressMapper userAddressMapper,
      final Auth0Util auth0Util) {
    this.userAddressService = userAddressService;
    this.userService = userService;
    this.userAddressMapper = userAddressMapper;
    this.auth0Util = auth0Util;
  }

  @PatchMapping("user-addresses/{id}")
  @PreAuthorize(
      "hasPermission(#id,'USER_ADDRESS', 'EDIT_USER')"
          + " or hasPermission(#id,'USER_ADDRESS', 'EDIT_SELF')")
  public UserAddressDto updateUserAddress(
      @PathVariable @HashidsFormat final Long id,
      @RequestBody final UserAddressDto userAddressDto) {
    final UserAddress origin = userAddressService.findUserAddressById(id);
    userAddressMapper.updateFromUserAddressDto(origin, userAddressDto);
    final UserAddress userAddressUpdated = userAddressService.updateUserAddress(origin);
    return userAddressMapper.convertToUserAddressDto(userAddressUpdated);
  }

  @PostMapping("users/{id}/user-address")
  @PreAuthorize(
      "hasPermission(#id,'USER', 'EDIT_USER')"
          + " or hasPermission(#id,'USER', 'EDIT_SELF')")
  public UserAddressDto saveUserAddress(@PathVariable @HashidsFormat final Long id,
      @RequestBody final UserAddressDto userAddressDto) {
    userAddressDto.setUserId(id);
    final UserAddress initUserAddress = userAddressService
        .save(userAddressMapper
            .createFromUserAddressDto(userAddressDto));
    return userAddressMapper.convertToUserAddressDto(initUserAddress);
  }

  @GetMapping("users/{id}/user-address")
  @PreAuthorize(
      "hasPermission(#id, 'USER', 'VIEW_USER_ADDRESS')"
          + "or hasPermission(#id, 'USER', 'VIEW_SELF')")
  public UserAddressDto getUserAddress(@PathVariable @HashidsFormat final Long id) {
    final User targetUser = userService.findUserById(id);
    final User manager = targetUser.getManagerUser();
    final UserAddress userAddress = userAddressService.findUserAddressByUserId(id);

    final User.Role userRole = auth0Util.getUserRole(getAuthUser().getEmail());
    if (userAddress != null && (getAuthUser().getId().equals(id)
        || userRole == Role.ADMIN
        || (manager != null && manager.getId().equals(getAuthUser().getId())))) {
      return userAddressMapper.convertToUserAddressDto(userAddress);
    }

    return null;
  }
}
