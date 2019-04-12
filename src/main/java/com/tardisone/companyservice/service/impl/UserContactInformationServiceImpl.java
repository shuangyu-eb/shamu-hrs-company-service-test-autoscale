package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.dto.UserContactInformationDTO;
import com.tardisone.companyservice.entity.UserContactInformation;
import com.tardisone.companyservice.repository.UserContactInformationRepository;
import com.tardisone.companyservice.service.UserContactInformationService;
import com.tardisone.companyservice.utils.PersonalInformationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserContactInformationServiceImpl implements UserContactInformationService {
    @Autowired
    UserContactInformationRepository repository;

    @Override
    public UserContactInformationDTO update(UserContactInformation userContactInformation) {
        UserContactInformation userContactInformationUpdated = repository.save(userContactInformation);
        UserContactInformationDTO userContactInformationDTO = PersonalInformationUtil.convertUserContactInfoEntityToDTO(userContactInformationUpdated);
        return userContactInformationDTO;
    }

}
