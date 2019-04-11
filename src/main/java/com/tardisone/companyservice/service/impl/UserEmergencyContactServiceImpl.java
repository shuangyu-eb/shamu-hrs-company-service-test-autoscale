package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.dto.UserEmergencyContactDTO;
import com.tardisone.companyservice.entity.State;
import com.tardisone.companyservice.entity.UserEmergencyContact;
import com.tardisone.companyservice.repository.UserEmergencyContactRepository;
import com.tardisone.companyservice.service.UserEmergencyContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserEmergencyContactServiceImpl implements UserEmergencyContactService {

	@Autowired
	UserEmergencyContactRepository userEmergencyContactRepository;

	@Override
	public List<UserEmergencyContactDTO> getUserEmergencyContacts(Long id) {
		List<UserEmergencyContact> userEmergencyContactList = userEmergencyContactRepository.findByUserId(id);
		return userEmergencyContactList.stream().map(userEmergencyContact -> {
			State state = userEmergencyContact.getState();
			Long stateId = state != null ? state.getId() : null;
			UserEmergencyContactDTO userEmergencyContactDTO = new UserEmergencyContactDTO(
			userEmergencyContact.getFirstName(),
			userEmergencyContact.getLastName(),
			userEmergencyContact.getRelationship(),
			userEmergencyContact.getPhone(),
			userEmergencyContact.getEmail(),
			userEmergencyContact.getStreet1(),
			userEmergencyContact.getStreet2(),
			userEmergencyContact.getCity(),
			stateId,
			userEmergencyContact.getPostalCode(),
			userEmergencyContact.getIsPrimary());
			return userEmergencyContactDTO;
		}).collect(Collectors.toList());
	}

	@Override
	public void createUserEmergencyContact(UserEmergencyContact userEmergencyContact) {
		if (userEmergencyContact.getIsPrimary()) {
			userEmergencyContactRepository.releasePrimaryContact(userEmergencyContact.getUser().getId());
		}
		userEmergencyContactRepository.save(userEmergencyContact);
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
	public void updateEmergencyContact(UserEmergencyContact userEmergencyContact) {
		if (userEmergencyContact.getIsPrimary()) {
			userEmergencyContactRepository.releasePrimaryContact(userEmergencyContact.getUser().getId());
		}
		userEmergencyContactRepository.save(userEmergencyContact);
	}
}
