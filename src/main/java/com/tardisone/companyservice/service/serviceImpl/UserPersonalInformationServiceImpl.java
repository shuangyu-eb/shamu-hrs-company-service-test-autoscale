package com.tardisone.companyservice.service.serviceImpl;

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
    private UserPersonalInformationRepository userPersonalInformationRepository;

    @Override
    public void addUserPersonalInformation(UserPersonalInformation userPersonalInformation) {
        userPersonalInformationRepository.save(userPersonalInformation);
    }

    @Override
    public UserPersonalInformation updateUserPersonalInformation(UserPersonalInformation userPersonalInformation) {
        Optional<UserPersonalInformation> optional = userPersonalInformationRepository.findById(userPersonalInformation.getId());
        UserPersonalInformation userInfoUpdate = optional.get();
        return null;
    }

    @Override
    public List<UserPersonalInformation> getUserPersonalInformations() {
        return null;
    }
}
