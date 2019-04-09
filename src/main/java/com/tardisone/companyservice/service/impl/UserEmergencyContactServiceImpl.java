package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.UserEmergencyContact;
import com.tardisone.companyservice.repository.UserEmergencyContactRepository;
import com.tardisone.companyservice.service.UserEmergencyContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserEmergencyContactServiceImpl implements UserEmergencyContactService {

	@Autowired
	UserEmergencyContactRepository userEmergencyContactRepository;

	@Override
	public List<UserEmergencyContact> getUserEmergencyContacts(Long id) {
		return userEmergencyContactRepository.findByUserId(id);
	}

	@Override
	public void deleteEmergencyContact(Long id) {
		Optional<UserEmergencyContact> userEmergencyContact = userEmergencyContactRepository.findById(id);
		if (userEmergencyContact.get().getIsPrimary()) {
			userEmergencyContactRepository.resetPrimaryContact(userEmergencyContact.get().getUser().getId());
		}
		userEmergencyContactRepository.delete(id);
	}

	@Override
	public void updateEmergencyContact(UserEmergencyContact emergencyContact) {
		if (emergencyContact.getIsPrimary()) {
			userEmergencyContactRepository.releasePrimaryContact(emergencyContact.getUser().getId());
		}
		userEmergencyContactRepository.save(emergencyContact);
	}
}
