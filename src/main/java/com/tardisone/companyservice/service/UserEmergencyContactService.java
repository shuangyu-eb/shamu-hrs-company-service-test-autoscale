package com.tardisone.companyservice.service;

import com.tardisone.companyservice.dto.UserEmergencyContactDTO;
import com.tardisone.companyservice.entity.UserEmergencyContact;

import java.util.List;

public interface UserEmergencyContactService {

	List<UserEmergencyContactDTO> getUserEmergencyContacts(Long id);

	void createUserEmergencyContact(UserEmergencyContact userEmergencyContact);

	void deleteEmergencyContact(Long id);

	void updateEmergencyContact(UserEmergencyContact emergencyContact);
}
