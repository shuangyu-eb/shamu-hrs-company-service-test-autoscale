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
  public UserPersonalInformationDto update(UserPersonalInformationDto userPersonalInformationDto) {
    UserPersonalInformation userPersonalInformation = userPersonalInformationDto
        .convertUserPersonalInformationDtoToEntity(userPersonalInformationDto);
    UserPersonalInformation userPersonalInformationUpdated =
        repository.save(userPersonalInformation);
    UserPersonalInformationDto userPersonalInformationDtoUpdated =
        new UserPersonalInformationDto(userPersonalInformationUpdated);
    return userPersonalInformationDtoUpdated;
  }
}
