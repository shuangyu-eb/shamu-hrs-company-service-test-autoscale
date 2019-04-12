package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.dto.UserPersonalInformationDTO;
import com.tardisone.companyservice.entity.UserPersonalInformation;
import com.tardisone.companyservice.repository.UserPersonalInformationRepository;
import com.tardisone.companyservice.service.UserPersonalInformationService;
import com.tardisone.companyservice.utils.PersonalInformationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserPersonalInformationServiceImpl implements UserPersonalInformationService{

    @Autowired
    UserPersonalInformationRepository repository;

    @Override
    public UserPersonalInformationDTO update(UserPersonalInformation userPersonalInformation) {

        UserPersonalInformation userPersonalInformationUpdated =  repository.save(userPersonalInformation);

        UserPersonalInformationDTO userPersonalInformationDTO = PersonalInformationUtil.convertUserPersonalInfoEntityToDTO(userPersonalInformationUpdated);

        return userPersonalInformationDTO;
    }
}
