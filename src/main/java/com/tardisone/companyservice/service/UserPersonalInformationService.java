package com.tardisone.companyservice.service;

import com.tardisone.companyservice.entity.UserPersonalInformation;

public interface UserPersonalInformationService {
    UserPersonalInformation findUserPersonalInformationByEmailWork(String emailWork);
    void updateUserPersonalInformation(UserPersonalInformation userPersonalInformation);
}
