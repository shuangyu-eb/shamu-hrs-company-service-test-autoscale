package com.tardisone.companyservice.service;

import com.tardisone.companyservice.entity.UserEmergencyContact;

import java.util.List;

public interface UserEmergencyContactService {

	List<UserEmergencyContact> getUserEmergencyContacts(Long id);

	void deleteEmergencyContact(Long id);

	void updateEmergencyContact(UserEmergencyContact emergencyContact);
}
