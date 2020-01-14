package shamu.company.user.controller;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.crypto.EncryptorUtil;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.user.dto.BasicUserPersonalInformationDto;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.dto.UserRoleAndStatusInfoDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.service.UserPersonalInformationService;
import shamu.company.user.service.UserService;

@RestApiController
public class UserPersonalInformationRestController extends BaseRestController {

  private final UserPersonalInformationService userPersonalInformationService;

  private final UserService userService;

  private final UserPersonalInformationMapper userPersonalInformationMapper;

  private final UserMapper userMapper;

  private final Auth0Helper auth0Helper;

  private final EncryptorUtil encryptorUtil;

  @Autowired
  public UserPersonalInformationRestController(
      final UserPersonalInformationService userPersonalInformationService,
      final UserService userService,
      final UserPersonalInformationMapper userPersonalInformationMapper,
      final UserMapper userMapper,
      final Auth0Helper auth0Helper,
      final EncryptorUtil encryptorUtil) {
    this.userPersonalInformationService = userPersonalInformationService;
    this.userService = userService;
    this.userPersonalInformationMapper = userPersonalInformationMapper;
    this.userMapper = userMapper;
    this.auth0Helper = auth0Helper;
    this.encryptorUtil = encryptorUtil;
  }

  @PatchMapping("user-personal-information/{id}")
  @PreAuthorize(
      "hasPermission(#id,'USER_PERSONAL_INFORMATION', 'EDIT_USER')"
          + " or hasPermission(#id,'USER_PERSONAL_INFORMATION', 'EDIT_SELF')")
  public UserPersonalInformationDto update(
      @PathVariable final String id,
      @Valid @RequestBody final UserPersonalInformationDto userPersonalInformationDto) {
    final User user = userService.findUserByUserPersonalInformationId(id);
    final UserPersonalInformation origin = user.getUserPersonalInformation();
    userPersonalInformationMapper
        .updateFromUserPersonalInformationDto(origin, userPersonalInformationDto);
    encryptorUtil.encryptSsn(user.getId(), userPersonalInformationDto.getSsn(), origin);

    final UserPersonalInformation userPersonalInformationUpdated =
        userPersonalInformationService.update(origin);
    return userPersonalInformationMapper
        .convertToUserPersonalInformationDto(userPersonalInformationUpdated);
  }

  @GetMapping("users/{id}/user-personal-information")
  @PreAuthorize(
      "hasPermission(#id, 'USER', 'VIEW_USER_PERSONAL')"
          + "or hasPermission(#id, 'USER', 'VIEW_SELF')")
  public BasicUserPersonalInformationDto getUserPersonalInformation(
      @PathVariable final String id) {
    final User targetUser = userService.findById(id);
    return userPersonalInformationService.findUserPersonalInformation(targetUser, findAuthUser());
  }

  @GetMapping("users/{id}/user-role-status")
  @PreAuthorize("hasPermission(#id, 'USER', 'VIEW_USER_ROLE_AND_STATUS')")
  public UserRoleAndStatusInfoDto getUserRoleAndStatus(@PathVariable final String id) {
    final User targetUser = userService.findById(id);
    final UserRoleAndStatusInfoDto resultInformation = userMapper
        .convertToUserRoleAndStatusInfoDto(targetUser);
    final Role userRole = auth0Helper.getUserRole(targetUser);
    resultInformation.setUserRole(userRole.getValue());
    return resultInformation;
  }
}
