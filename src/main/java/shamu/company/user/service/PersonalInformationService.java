package shamu.company.user.service;

import shamu.company.user.dto.PersonalInformationDTO;

public interface PersonalInformationService {
    PersonalInformationDTO getPersonalInformation(Long userId);
}
