package shamu.company.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.repository.UserPersonalInformationRepository;
import shamu.company.user.service.UserPersonalInformationService;

@Service
public class UserPersonalInformationServiceImpl implements UserPersonalInformationService {

  @Autowired
  UserPersonalInformationRepository repository;

  @Override
  public UserPersonalInformation update(UserPersonalInformation userPersonalInformation) {
    UserPersonalInformation userPersonalInformationUpdated =
        repository.save(userPersonalInformation);
    return userPersonalInformationUpdated;
  }
}
