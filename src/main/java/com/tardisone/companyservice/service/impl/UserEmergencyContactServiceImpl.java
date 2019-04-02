package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.UserEmergencyContact;
import com.tardisone.companyservice.repository.UserEmergencyContactRepository;
import com.tardisone.companyservice.service.UserEmergencyContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserEmergencyContactServiceImpl implements UserEmergencyContactService {

	@Autowired
	UserEmergencyContactRepository userEmergencyContactRepository;

	@Override
	public List<UserEmergencyContact> getUserEmergencyContacts(Long id) {

		return userEmergencyContactRepository.findByUserId(id);
	}
}
