package com.tardisone.companyservice.service;

import com.tardisone.companyservice.entity.UserContactInformation;

public interface UserContactInformationService {
    UserContactInformation findUserContactInformationByEmailWork(String emailWork);
    void update(UserContactInformation userContactInformation);
    UserContactInformation findUserContactInformationById(Long id);
}
