package com.tardisone.companyservice.service;

import com.tardisone.companyservice.entity.UserPersonalInformation;

public interface UserPersonalInformationService {
    UserPersonalInformation findUserPersonalInformationById(Long id);
    UserPersonalInformation findUserPersonalInformationByFirstName(String firstName);
    void update(UserPersonalInformation userPersonalInformation);
}
