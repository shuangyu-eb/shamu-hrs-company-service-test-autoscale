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
import shamu.company.user.dto.BasicUserContactInformationDto;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.mapper.UserContactInformationMapper;
import shamu.company.user.service.UserContactInformationService;
import shamu.company.user.service.UserService;

@RestApiController
public class UserContactInformationRestController extends BaseRestController {

  private final UserContactInformationService contactInformationService;

  private final UserService userService;

  private final UserContactInformationMapper userContactInformationMapper;

  @Autowired
  public UserContactInformationRestController(
      final UserContactInformationService contactInformationService, final UserService userService,
      final UserContactInformationMapper userContactInformationMapper) {
    this.contactInformationService = contactInformationService;
    this.userService = userService;
    this.userContactInformationMapper = userContactInformationMapper;
  }

  @PatchMapping("user-contact-information/{id}")
  @PreAuthorize(
      "hasPermission(#id,'USER_CONTACT_INFORMATION', 'EDIT_USER')"
          + " or hasPermission(#id,'USER_CONTACT_INFORMATION', 'EDIT_SELF')")
  public UserContactInformationDto update(
      @PathVariable final String id,
      @Valid @RequestBody final UserContactInformationDto userContactInformationDto) {
    final UserContactInformation origin = contactInformationService
        .findUserContactInformationById(id);
    userContactInformationMapper
        .updateFromUserContactInformationDto(origin, userContactInformationDto);
    final UserContactInformation userContactInformationUpdated =
        contactInformationService.update(origin);
    return userContactInformationMapper
        .convertToUserContactInformationDto(userContactInformationUpdated);
  }

  @GetMapping("users/{id}/user-contact-information")
  @PreAuthorize(
      "hasPermission(#id, 'USER', 'VIEW_USER_CONTACT')"
          + "or hasPermission(#id, 'USER', 'VIEW_SELF')")
  public BasicUserContactInformationDto getUserContactInformation(
      @PathVariable final String id) {
    final User targetUser = userService.findById(id);
    final User manager = targetUser.getManagerUser();
    final UserContactInformation userContactInformation = targetUser.getUserContactInformation();

    final User currentUser = userService.findByUserId(findUserId());
    final Role userRole = currentUser.getRole();
    if (findAuthUser().getId().equals(id)
        || (manager != null && manager.getId().equals(findAuthUser().getId()))
        || userRole == Role.ADMIN) {
      return userContactInformationMapper
          .convertToUserContactInformationDto(userContactInformation);
    }

    return new BasicUserContactInformationDto(userContactInformation);
  }
}
