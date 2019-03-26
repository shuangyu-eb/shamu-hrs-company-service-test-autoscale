package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.UserContactInformation;
import com.tardisone.companyservice.repository.UserContactInformationRepository;
import com.tardisone.companyservice.service.UserContactInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserContactInformationServiceImpl implements UserContactInformationService {
    @Autowired
    UserContactInformationRepository repository;

    @Override
    public UserContactInformation findUserContactInformationByEmailWork(String emailWork) {
        return repository.findUserContactInformationByEmailWork(emailWork);
    }

    @Override
    public void update(UserContactInformation userContactInformation) {
        repository.save(userContactInformation);
    }
}
