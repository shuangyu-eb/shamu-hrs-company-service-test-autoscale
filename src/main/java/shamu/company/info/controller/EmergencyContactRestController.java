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
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.service.UserService;

@RestApiController
public class EmergencyContactRestController extends BaseRestController {

  @Autowired
  UserEmergencyContactService userEmergencyContactService;

  @Autowired
  UserService userService;

  @GetMapping("users/{userId}/user-emergency-contacts")
  @PreAuthorize("hasPermission(#userId,'USER', 'VIEW_USER_EMERGENCY_CONTACT')"
      + " or hasPermission(#userId,'USER', 'VIEW_SELF')")
  public List<BasicUserEmergencyContactDto> getEmergencyContacts(
      @PathVariable @HashidsFormat Long userId) {
    User user = this.getUser();
    List<UserEmergencyContact> userEmergencyContacts = userEmergencyContactService
        .getUserEmergencyContacts(userId);

    if (user.getId() == userId || user.getRole() == Role.ADMIN) {
      return userEmergencyContacts.stream()
          .map(UserEmergencyContactDto::new)
          .collect(Collectors.toList());
    }
    return userEmergencyContacts.stream()
        .map(BasicUserEmergencyContactDto::new)
        .collect(Collectors.toList());
  }

  @PostMapping("users/{userId}/user-emergency-contacts")
  @PreAuthorize("hasPermission(#userId,'USER', 'EDIT_USER')"
      + " or hasPermission(#userId,'USER', 'EDIT_SELF')")
  public HttpEntity createEmergencyContacts(@PathVariable @HashidsFormat Long userId,
      @RequestBody UserEmergencyContact emergencyContact) {
    User user = userService.getOne(userId);
    emergencyContact.setUser(user);
    checkEmergencyContactState(emergencyContact);
    userEmergencyContactService.createUserEmergencyContact(userId, emergencyContact);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @DeleteMapping("users/{userId}/user-emergency-contacts/{id}")
  @PreAuthorize("hasPermission(#userId,'USER', 'EDIT_USER')"
      + " or hasPermission(#userId,'USER', 'EDIT_SELF')")
  public HttpEntity deleteEmergencyContacts(@PathVariable @HashidsFormat Long userId,
      @PathVariable @HashidsFormat Long id) {
    userEmergencyContactService.deleteEmergencyContact(userId, id);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PatchMapping("users/{userId}/user-emergency-contacts")
  @PreAuthorize("hasPermission(#userId,'USER', 'EDIT_USER')"
      + " or hasPermission(#userId,'USER', 'EDIT_SELF')")
  public HttpEntity updateEmergencyContact(@PathVariable @HashidsFormat Long userId,
      @RequestBody UserEmergencyContact userEmergencyContact) {
    User user = userService.getOne(userId);
    userEmergencyContact.setUser(user);
    checkEmergencyContactState(userEmergencyContact);
    userEmergencyContactService.updateEmergencyContact(userId, userEmergencyContact);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  private void checkEmergencyContactState(UserEmergencyContact userEmergencyContact) {
    if (userEmergencyContact.getState().getId() == null) {
      userEmergencyContact.setState(null);
    }
  }
}
