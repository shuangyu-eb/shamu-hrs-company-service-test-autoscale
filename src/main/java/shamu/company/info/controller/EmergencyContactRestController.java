package shamu.company.info.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.info.dto.BasicUserEmergencyContactDto;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.entity.mapper.UserEmergencyContactMapper;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.service.UserService;

@RestApiController
public class EmergencyContactRestController extends BaseRestController {

  private final UserEmergencyContactService userEmergencyContactService;

  private final UserService userService;

  private final UserEmergencyContactMapper userEmergencyContactMapper;

  @Autowired
  public EmergencyContactRestController(
      final UserEmergencyContactService userEmergencyContactService,
      final UserService userService,
      final UserEmergencyContactMapper userEmergencyContactMapper) {
    this.userEmergencyContactService = userEmergencyContactService;
    this.userService = userService;
    this.userEmergencyContactMapper = userEmergencyContactMapper;
  }

  @GetMapping("users/{userId}/user-emergency-contacts")
  @PreAuthorize(
      "hasPermission(#userId,'USER', 'VIEW_USER_EMERGENCY_CONTACT')"
          + " or hasPermission(#userId,'USER', 'VIEW_SELF')")
  public List<BasicUserEmergencyContactDto> getEmergencyContacts(
      @PathVariable final String userId) {
    final List<UserEmergencyContact> userEmergencyContacts =
        userEmergencyContactService.findUserEmergencyContacts(userId);
    return convertToUserEmergencyContactDtoByPermission(userId, userEmergencyContacts);
  }

  @PostMapping("users/{userId}/user-emergency-contacts")
  @PreAuthorize(
      "hasPermission(#userId,'USER', 'EDIT_USER')"
          + " or hasPermission(#userId,'USER', 'EDIT_SELF')")
  public HttpEntity createEmergencyContacts(
      @PathVariable final String userId,
      @RequestBody final UserEmergencyContactDto emergencyContactDto) {
    emergencyContactDto.setUserId(userId);
    final UserEmergencyContact userEmergencyContact =
        userEmergencyContactMapper.createFromUserEmergencyContactDto(emergencyContactDto);
    checkEmergencyContactState(userEmergencyContact);
    userEmergencyContactService.createUserEmergencyContact(userId, userEmergencyContact);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @DeleteMapping("user-emergency-contacts/{id}")
  @PreAuthorize(
      "hasPermission(#id, 'USER_EMERGENCY_CONTACT', 'EDIT_SELF')"
          + "or hasPermission(#id, 'USER_EMERGENCY_CONTACT', 'EDIT_USER')")
  public HttpEntity deleteEmergencyContacts(@PathVariable final String id) {
    userEmergencyContactService.deleteEmergencyContact(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PatchMapping("user-emergency-contacts")
  @PreAuthorize(
      "hasPermission(#userEmergencyContactDto.id, 'USER_EMERGENCY_CONTACT', 'EDIT_SELF')"
          + "or hasPermission(#userEmergencyContactDto.id, 'USER_EMERGENCY_CONTACT', 'EDIT_USER')")
  public HttpEntity updateEmergencyContact(
      @RequestBody final UserEmergencyContactDto userEmergencyContactDto) {
    userEmergencyContactService.updateEmergencyContact(userEmergencyContactDto);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  private void checkEmergencyContactState(final UserEmergencyContact userEmergencyContact) {
    if (userEmergencyContact.getState() != null
        && userEmergencyContact.getState().getId() == null) {
      userEmergencyContact.setState(null);
    }
  }

  private List<BasicUserEmergencyContactDto> convertToUserEmergencyContactDtoByPermission(
      final String userId, final List<UserEmergencyContact> userEmergencyContacts) {
    final User currentUser = userService.findActiveUserById(findUserId());
    if (userId.equals(findAuthUser().getId())
        || Role.ADMIN.equals(currentUser.getRole())
        || Role.SUPER_ADMIN.equals(currentUser.getRole())) {
      return userEmergencyContacts.stream()
          .map(userEmergencyContactMapper::convertToUserEmergencyContactDto)
          .collect(Collectors.toList());
    }
    return userEmergencyContacts.stream()
        .map(userEmergencyContactMapper::convertToBasicUserEmergencyContactDto)
        .collect(Collectors.toList());
  }
}
