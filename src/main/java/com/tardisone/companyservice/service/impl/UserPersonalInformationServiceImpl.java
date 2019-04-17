package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.UserPersonalInformation;
import com.tardisone.companyservice.repository.UserPersonalInformationRepository;
import com.tardisone.companyservice.service.UserPersonalInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("userPersonalInformationService")
public class UserPersonalInformationServiceImpl implements UserPersonalInformationService {

    @Autowired
    UserPersonalInformationRepository repository;

    @Override
    public UserPersonalInformation findUserPersonalInformationById(Long id) {
        return repository.findUserPersonalInformationById(id);
    }

    @Override
    public UserPersonalInformation findUserPersonalInformationByFirstName(String firstName) {
        return repository.findUserPersonalInformationByFirstName(firstName);
    }

    @Override
    public void update(UserPersonalInformation userPersonalInformation) {
        repository.save(userPersonalInformation);
    }
}
