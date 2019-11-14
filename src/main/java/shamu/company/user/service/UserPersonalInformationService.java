package shamu.company.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.repository.UserPersonalInformationRepository;

@Service
public class UserPersonalInformationService {

  private final UserPersonalInformationRepository repository;

  private final GenderService genderService;

  private final MaritalStatusService maritalStatusService;

  @Autowired
  public UserPersonalInformationService(
      final UserPersonalInformationRepository repository,
      final GenderService genderService,
      final MaritalStatusService maritalStatusService) {
    this.repository = repository;
    this.genderService = genderService;
    this.maritalStatusService = maritalStatusService;
  }

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

  public UserPersonalInformation findUserPersonalInformationById(final String id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("User personal information does not exist"));
  }
}
