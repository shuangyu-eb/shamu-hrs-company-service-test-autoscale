package shamu.company.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.repository.UserPersonalInformationRepository;
import shamu.company.user.service.GenderService;
import shamu.company.user.service.MaritalStatusService;
import shamu.company.user.service.UserPersonalInformationService;

@Service
public class UserPersonalInformationServiceImpl implements UserPersonalInformationService {

  private final UserPersonalInformationRepository repository;

  private final GenderService genderService;

  private final MaritalStatusService maritalStatusService;

  @Autowired
  public UserPersonalInformationServiceImpl(
      final UserPersonalInformationRepository repository,
      final GenderService genderService,
      final MaritalStatusService maritalStatusService) {
    this.repository = repository;
    this.genderService = genderService;
    this.maritalStatusService = maritalStatusService;
  }

  @Override
  public UserPersonalInformation update(final UserPersonalInformation userPersonalInformation) {
    final Gender gender = userPersonalInformation.getGender();
    final MaritalStatus maritalStatus = userPersonalInformation.getMaritalStatus();

    if (gender != null) {
      final Gender genderUpdated = genderService.findGenderById(gender.getId());
      userPersonalInformation.setGender(genderUpdated);
    }
    if (maritalStatus != null) {
      final MaritalStatus maritalStatusUpdated =
          maritalStatusService.findMaritalStatusById(maritalStatus.getId());
      userPersonalInformation.setMaritalStatus(maritalStatusUpdated);
    }
    return repository.save(userPersonalInformation);
  }

  @Override
  public UserPersonalInformation findUserPersonalInformationById(final Long id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("User personal information does not exist"));
  }
}
