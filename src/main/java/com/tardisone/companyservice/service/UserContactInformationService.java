package com.tardisone.companyservice.service;

import com.tardisone.companyservice.dto.UserContactInformationDTO;
import com.tardisone.companyservice.entity.UserContactInformation;

public interface UserContactInformationService {
    UserContactInformationDTO update(UserContactInformation userContactInformation);
}
