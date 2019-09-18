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
import shamu.company.hashids.HashidsFormat;
import shamu.company.info.dto.BasicUserEmergencyContactDto;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.entity.mapper.UserEmergencyContactMapper;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.service.UserService;
import shamu.company.utils.Auth0Util;

@RestApiController
public class EmergencyContactRestController extends BaseRestController {

  private final UserEmergencyContactService userEmergencyContactService;

  private final UserService userService;

  private final UserEmergencyContactMapper userEmergencyContactMapper;

  private final Auth0Util auth0Util;

  @Autowired
  public EmergencyContactRestController(
      final UserEmergencyContactService userEmergencyContactService,
      final UserService userService,
      final UserEmergencyContactMapper userEmergencyContactMapper,
      final Auth0Util auth0Util) {
    this.userEmergencyContactService = userEmergencyContactService;
    this.userService = userService;
    this.userEmergencyContactMapper = userEmergencyContactMapper;
    this.auth0Util = auth0Util;
  }

  @GetMapping("users/{userId}/user-emergency-contacts")
  @PreAuthorize("hasPermission(#userId,'USER', 'VIEW_USER_EMERGENCY_CONTACT')"
      + " or hasPermission(#userId,'USER', 'VIEW_SELF')")
  public List<BasicUserEmergencyContactDto> getEmergencyContacts(
      @PathVariable @HashidsFormat final Long userId) {
    final List<UserEmergencyContact> userEmergencyContacts = userEmergencyContactService
        .getUserEmergencyContacts(userId);

    final Role userRole = auth0Util.getUserRole(getAuthUser().getEmail());
    if (userId.equals(getAuthUser().getId())
        || Role.ADMIN.equals(userRole)) {
      return userEmergencyContacts.stream()
          .map(userEmergencyContactMapper::convertToUserEmergencyContactDto)
          .collect(Collectors.toList());
    }
    return userEmergencyContacts.stream()
        .map(userEmergencyContactMapper::convertToBasicUserEmergencyContactDto)
        .collect(Collectors.toList());
  }

  @PostMapping("users/{userId}/user-emergency-contacts")
  @PreAuthorize("hasPermission(#userId,'USER', 'EDIT_USER')"
      + " or hasPermission(#userId,'USER', 'EDIT_SELF')")
  public HttpEntity createEmergencyContacts(@PathVariable @HashidsFormat final Long userId,
      @RequestBody final UserEmergencyContactDto emergencyContactDto) {
    final User user = userService.getOne(userId);
    final UserEmergencyContact userEmergencyContact = userEmergencyContactMapper
        .createFromUserEmergencyContactDto(emergencyContactDto);
    userEmergencyContact.setUser(user);
    checkEmergencyContactState(userEmergencyContact);
    userEmergencyContactService.createUserEmergencyContact(userId, userEmergencyContact);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @DeleteMapping("users/{userId}/user-emergency-contacts/{id}")
  @PreAuthorize("hasPermission(#userId,'USER', 'EDIT_USER')"
      + " or hasPermission(#userId,'USER', 'EDIT_SELF')")
  public HttpEntity deleteEmergencyContacts(@PathVariable @HashidsFormat final Long userId,
      @PathVariable @HashidsFormat final Long id) {
    userEmergencyContactService.deleteEmergencyContact(userId, id);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PatchMapping("users/{userId}/user-emergency-contacts")
  @PreAuthorize("hasPermission(#userId,'USER', 'EDIT_USER')"
      + " or hasPermission(#userId,'USER', 'EDIT_SELF')")
  public HttpEntity updateEmergencyContact(@PathVariable @HashidsFormat final Long userId,
      @RequestBody final UserEmergencyContactDto userEmergencyContactDto) {
    final User user = userService.getOne(userId);
    final UserEmergencyContact userEmergencyContact = userEmergencyContactMapper
        .createFromUserEmergencyContactDto(userEmergencyContactDto);
    userEmergencyContact.setUser(user);
    checkEmergencyContactState(userEmergencyContact);
    userEmergencyContactService.updateEmergencyContact(userId, userEmergencyContact);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  private void checkEmergencyContactState(final UserEmergencyContact userEmergencyContact) {
    if (userEmergencyContact.getState().getId() == null) {
      userEmergencyContact.setState(null);
    }
  }
}
