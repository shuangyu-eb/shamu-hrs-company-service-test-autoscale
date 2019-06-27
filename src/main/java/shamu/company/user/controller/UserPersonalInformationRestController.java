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
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.UserPersonalInformationService;
import shamu.company.user.service.UserService;

@RestApiController
public class UserPersonalInformationRestController extends BaseRestController {

  private final UserPersonalInformationService userPersonalInformationService;

  private final UserService userService;

  SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");

  @Autowired
  public UserPersonalInformationRestController(
      UserPersonalInformationService userPersonalInformationService, UserService userService) {
    this.userPersonalInformationService = userPersonalInformationService;
    this.userService = userService;
  }

  @PatchMapping("user-personal-information/{id}")
  @PreAuthorize(
      "hasPermission(#id,'USER_PERSONAL_INFORMATION', 'EDIT_USER')"
          + " or hasPermission(#id,'USER_PERSONAL_INFORMATION', 'EDIT_SELF')")
  public UserPersonalInformationDto update(
      @PathVariable @HashidsFormat Long id,
      @RequestBody UserPersonalInformationDto userPersonalInformationDto) {
    UserPersonalInformation origin =
        userPersonalInformationService.findUserPersonalInformationById(id);
    UserPersonalInformation userPersonalInformation =
        userPersonalInformationDto.getUserPersonalInformation(origin);
    UserPersonalInformation userPersonalInformationUpdated =
        userPersonalInformationService.update(userPersonalInformation);
    return new UserPersonalInformationDto(userPersonalInformationUpdated);
  }

  @GetMapping("users/{id}/user-personal-information")
  @PreAuthorize("hasPermission(#id, 'USER', 'VIEW_USER_PERSONAL')")
  public BasicUserPersonalInformationDto getUserPersonalInformation(
      @PathVariable @HashidsFormat Long id) {
    User user = this.getUser();
    User targetUser = userService.findUserById(id);
    UserPersonalInformation userPersonalInformation = targetUser.getUserPersonalInformation();

    if (user.getId().equals(id) || user.getRole() == Role.ADMIN) {
      return new UserPersonalInformationDto(userPersonalInformation);
    }

    Date birthDate = userPersonalInformation.getBirthDate();
    String birthDateWithoutYear = birthDate != null ? sdf.format(birthDate) : "";
    BasicUserPersonalInformationDto basicUserPersonalInformationDto =
        new BasicUserPersonalInformationDto(userPersonalInformation);
    basicUserPersonalInformationDto.setBirthDate(birthDateWithoutYear);
    return basicUserPersonalInformationDto;
  }
}
