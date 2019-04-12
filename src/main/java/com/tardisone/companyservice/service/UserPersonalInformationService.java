package com.tardisone.companyservice.service;

import com.tardisone.companyservice.dto.UserPersonalInformationDTO;
import com.tardisone.companyservice.entity.UserPersonalInformation;

public interface UserPersonalInformationService {
    UserPersonalInformationDTO update(UserPersonalInformation userPersonalInformation);
}
