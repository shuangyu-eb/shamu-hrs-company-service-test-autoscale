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

	@GetMapping("users/{id}/user-emergency-contacts")
	public List<UserEmergencyContactDTO> getEmergencyContacts(@PathVariable Long id) {
		List<UserEmergencyContact> userEmergencyContacts = userEmergencyContactService.getUserEmergencyContacts(id);
		List<UserEmergencyContactDTO> userEmergencyContactDTOS = userEmergencyContacts.stream()
			.map(userEmergencyContact -> new UserEmergencyContactDTO(userEmergencyContact)).collect(Collectors.toList());
		return userEmergencyContactDTOS;
	}

	@PostMapping("user-emergency-contacts")
	public HttpEntity createEmergencyContacts(@RequestBody UserEmergencyContact emergencyContact) {
		userEmergencyContactService.createUserEmergencyContact(emergencyContact);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping("user-emergency-contacts/{id}")
	public HttpEntity deleteEmergencyContacts(@PathVariable Long id) {
		userEmergencyContactService.deleteEmergencyContact(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PatchMapping("user-emergency-contacts")
	public HttpEntity updateEmergencyContact(@RequestBody UserEmergencyContact userEmergencyContact) {
		userEmergencyContactService.updateEmergencyContact(userEmergencyContact);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
