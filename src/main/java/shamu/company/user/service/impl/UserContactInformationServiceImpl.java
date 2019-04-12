package shamu.company.user.service.impl;

import shamu.company.user.dto.UserContactInformationDTO;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.repository.UserContactInformationRepository;
import shamu.company.user.service.UserContactInformationService;
import shamu.company.utils.PersonalInformationUtil;
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
