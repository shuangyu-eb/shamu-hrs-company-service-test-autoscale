package com.tardisone.companyservice.controller.info;

import com.tardisone.companyservice.config.annotations.RestApiController;
import com.tardisone.companyservice.entity.UserEmergencyContact;
import com.tardisone.companyservice.service.UserEmergencyContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestApiController
public class EmergencyContactRestController {

	@Autowired
	UserEmergencyContactService userEmergencyContactService;

	@GetMapping("users/{id}/user-emergency-contacts")
	public List<UserEmergencyContact> getEmergencyContacts(@PathVariable Long id) {
		return userEmergencyContactService.getUserEmergencyContacts(id);
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
