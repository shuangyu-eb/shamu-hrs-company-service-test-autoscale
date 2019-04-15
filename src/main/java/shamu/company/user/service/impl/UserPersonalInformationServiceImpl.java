package shamu.company.user.service.impl;

import shamu.company.user.dto.UserPersonalInformationDTO;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.repository.UserPersonalInformationRepository;
import shamu.company.user.service.UserPersonalInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserPersonalInformationServiceImpl implements UserPersonalInformationService {

    @Autowired
    UserPersonalInformationRepository repository;

    @Override
    public UserPersonalInformationDTO update(UserPersonalInformationDTO userPersonalInformationDTO) {
        UserPersonalInformation userPersonalInformation = new UserPersonalInformation(userPersonalInformationDTO);
        UserPersonalInformation userPersonalInformationUpdated =  repository.save(userPersonalInformation);
        UserPersonalInformationDTO userPersonalInformationDTOUpdated = new UserPersonalInformationDTO(userPersonalInformationUpdated);
        return userPersonalInformationDTOUpdated;
    }
}
