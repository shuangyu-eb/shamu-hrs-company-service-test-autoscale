package shamu.company.info.controller;

import shamu.company.common.config.annotations.RestApiController;
import shamu.company.info.dto.UserEmergencyContactDTO;
import shamu.company.info.entity.UserEmergencyContact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shamu.company.info.service.UserEmergencyContactService;

import java.util.List;
import java.util.stream.Collectors;

@RestApiController
public class EmergencyContactRestController {

	@Autowired
	UserEmergencyContactService userEmergencyContactService;

	@GetMapping("users/{userId}/user-emergency-contacts")
	public List<UserEmergencyContactDTO> getEmergencyContacts(@PathVariable Long userId) {
		List<UserEmergencyContact> userEmergencyContacts = userEmergencyContactService.getUserEmergencyContacts(userId);
		List<UserEmergencyContactDTO> userEmergencyContactDTOS = userEmergencyContacts.stream()
			.map(userEmergencyContact -> new UserEmergencyContactDTO(userEmergencyContact)).collect(Collectors.toList());
		return userEmergencyContactDTOS;
	}

	@PostMapping("users/{userId}/user-emergency-contacts")
	public HttpEntity createEmergencyContacts(@PathVariable Long userId, @RequestBody UserEmergencyContact emergencyContact) {
		userEmergencyContactService.createUserEmergencyContact(userId, emergencyContact);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping("users/{userId}/user-emergency-contacts/{id}")
	public HttpEntity deleteEmergencyContacts(@PathVariable Long userId, @PathVariable Long id) {
		userEmergencyContactService.deleteEmergencyContact(userId, id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PatchMapping("users/{userId}/user-emergency-contacts")
	public HttpEntity updateEmergencyContact(@PathVariable Long userId, @RequestBody UserEmergencyContact userEmergencyContact) {
		userEmergencyContactService.updateEmergencyContact(userId, userEmergencyContact);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
