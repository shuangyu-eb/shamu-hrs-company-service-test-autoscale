package shamu.company.info.service;

import shamu.company.info.entity.UserEmergencyContact;

import java.util.List;

public interface UserEmergencyContactService {

	List<UserEmergencyContact> getUserEmergencyContacts(Long id);

	void createUserEmergencyContact(UserEmergencyContact userEmergencyContact);

	void deleteEmergencyContact(Long id);

	void updateEmergencyContact(UserEmergencyContact emergencyContact);
}
