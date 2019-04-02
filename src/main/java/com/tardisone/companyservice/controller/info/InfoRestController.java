package com.tardisone.companyservice.controller.info;

import com.tardisone.companyservice.entity.UserEmergencyContact;
import com.tardisone.companyservice.service.UserEmergencyContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/company")
public class InfoRestController {

	@Autowired
	UserEmergencyContactService userEmergencyContactService;

	@GetMapping("users/{id}/user-emergency-contacts")
	public List<UserEmergencyContact> getEmergencyContacts(@PathVariable Long id) {
		List<UserEmergencyContact> userEmergencyContacts = userEmergencyContactService.getUserEmergencyContacts(id);
		return userEmergencyContacts;
	}
}
