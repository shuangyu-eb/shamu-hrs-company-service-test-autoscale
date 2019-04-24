package shamu.company.info.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.info.entity.UserEmergencyContact;
import shamu.company.info.service.UserEmergencyContactService;

@RestApiController
public class EmergencyContactRestController extends BaseRestController {

  @Autowired
  UserEmergencyContactService userEmergencyContactService;

  @GetMapping("users/{userId}/user-emergency-contacts")
  public List<UserEmergencyContactDto> getEmergencyContacts(@PathVariable Long userId) {
    List<UserEmergencyContact> userEmergencyContacts = userEmergencyContactService
        .getUserEmergencyContacts(userId);

    return userEmergencyContacts.stream()
        .map(UserEmergencyContactDto::new)
        .collect(Collectors.toList());
  }

  @PostMapping("users/{userId}/user-emergency-contacts")
  public HttpEntity createEmergencyContacts(@PathVariable Long userId,
      @RequestBody UserEmergencyContact emergencyContact) {
    emergencyContact.setUserId(userId);
    userEmergencyContactService.createUserEmergencyContact(userId, emergencyContact);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @DeleteMapping("users/{userId}/user-emergency-contacts/{id}")
  public HttpEntity deleteEmergencyContacts(@PathVariable Long userId, @PathVariable Long id) {
    userEmergencyContactService.deleteEmergencyContact(userId, id);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PatchMapping("users/{userId}/user-emergency-contacts")
  public HttpEntity updateEmergencyContact(@PathVariable Long userId,
      @RequestBody UserEmergencyContact userEmergencyContact) {
    userEmergencyContactService.updateEmergencyContact(userId, userEmergencyContact);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
