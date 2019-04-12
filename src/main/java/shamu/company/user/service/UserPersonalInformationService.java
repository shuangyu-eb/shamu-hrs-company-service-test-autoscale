package shamu.company.user.service;

import shamu.company.user.dto.UserPersonalInformationDTO;
import shamu.company.user.entity.UserPersonalInformation;

public interface UserPersonalInformationService {
    UserPersonalInformationDTO update(UserPersonalInformation userPersonalInformation);
}
