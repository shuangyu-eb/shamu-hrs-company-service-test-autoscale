package shamu.company.user.controller;

import java.sql.Date;
import java.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.hashids.HashidsFormat;
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
import shamu.company.utils.Auth0Util;

@RestApiController
public class UserPersonalInformationRestController extends BaseRestController {

  private final UserPersonalInformationService userPersonalInformationService;

  private final UserService userService;

  private final UserPersonalInformationMapper userPersonalInformationMapper;

  private final UserMapper userMapper;

  private final Auth0Util auth0Util;

  SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");

  @Autowired
  public UserPersonalInformationRestController(
      final UserPersonalInformationService userPersonalInformationService,
      final UserService userService,
      final UserPersonalInformationMapper userPersonalInformationMapper,
      final UserMapper userMapper,
      final Auth0Util auth0Util) {
    this.userPersonalInformationService = userPersonalInformationService;
    this.userService = userService;
    this.userPersonalInformationMapper = userPersonalInformationMapper;
    this.userMapper = userMapper;
    this.auth0Util = auth0Util;
  }

  @PatchMapping("user-personal-information/{id}")
  @PreAuthorize(
      "hasPermission(#id,'USER_PERSONAL_INFORMATION', 'EDIT_USER')"
          + " or hasPermission(#id,'USER_PERSONAL_INFORMATION', 'EDIT_SELF')")
  public UserPersonalInformationDto update(
      @PathVariable @HashidsFormat final Long id,
      @RequestBody final UserPersonalInformationDto userPersonalInformationDto) {
    final UserPersonalInformation origin =
        userPersonalInformationService.findUserPersonalInformationById(id);
    userPersonalInformationMapper
        .updateFromUserPersonalInformationDto(origin, userPersonalInformationDto);
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
      @PathVariable @HashidsFormat final Long id) {
    final User targetUser = userService.findUserById(id);
    final UserPersonalInformation userPersonalInformation = targetUser.getUserPersonalInformation();
    final String imageUrl = targetUser.getImageUrl();

    final Role userRole = auth0Util.getUserRole(getAuthUser().getEmail());
    if (getAuthUser().getId().equals(id) || userRole == Role.ADMIN) {
      return userPersonalInformationMapper
          .convertToUserPersonalInformationDto(userPersonalInformation, imageUrl);
    }

    if (userRole == Role.MANAGER && targetUser.getManagerUser() != null
        && getAuthUser().getId().equals(targetUser.getManagerUser().getId())) {
      return userPersonalInformationMapper.convertToMyEmployeePersonalInformationDto(
            userPersonalInformation);

    }

    final Date birthDate = userPersonalInformation.getBirthDate();
    final String birthDateWithoutYear = birthDate != null ? sdf.format(birthDate) : "";
    final BasicUserPersonalInformationDto basicUserPersonalInformationDto =
        userPersonalInformationMapper
            .convertToBasicUserPersonalInformationDto(userPersonalInformation);
    basicUserPersonalInformationDto.setBirthDate(birthDateWithoutYear);
    return basicUserPersonalInformationDto;
  }

  @GetMapping("users/{id}/user-role-status")
  @PreAuthorize("hasPermission(#id, 'USER', 'VIEW_SELF')"
          + "or hasPermission(#id, 'USER', 'EDIT_USER')"
          + "or hasPermission(#id, 'USER', 'VIEW_MY_TEAM')"
          + "or hasPermission(#id, 'USER', 'VIEW_EMPLOYEES')")
  public UserRoleAndStatusInfoDto getUserRoleAndStatus(@PathVariable @HashidsFormat final Long id) {
    final User targetUser = userService.findUserById(id);
    final UserRoleAndStatusInfoDto resultInformation = userMapper
        .convertToUserRoleAndStatusInfoDto(targetUser);
    final Role userRole = auth0Util
        .getUserRole(targetUser.getUserContactInformation().getEmailWork());
    resultInformation.setUserRole(userRole.getValue());
    return resultInformation;
  }
}
