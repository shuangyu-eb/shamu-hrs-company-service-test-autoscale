package com.tardisone.companyservice.service;

import com.tardisone.companyservice.model.UserPersonalInformation;

import java.util.List;

public interface UserPersonalInformationService {

    public void addUserPersonalInformation(UserPersonalInformation userPersonalInformation);

    public UserPersonalInformation updateUserPersonalInformation(UserPersonalInformation userPersonalInformation);

    public List<UserPersonalInformation> getUserPersonalInformations();
}
