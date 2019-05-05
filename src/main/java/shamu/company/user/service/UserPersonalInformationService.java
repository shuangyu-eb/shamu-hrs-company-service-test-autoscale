package shamu.company.user.service;

import shamu.company.user.entity.UserPersonalInformation;

public interface UserPersonalInformationService {

  UserPersonalInformation update(UserPersonalInformation userPersonalInformation);

  UserPersonalInformation findUserPersonalInformationById(Long id);
}
