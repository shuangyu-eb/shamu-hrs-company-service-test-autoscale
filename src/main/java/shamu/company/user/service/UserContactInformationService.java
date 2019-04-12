package shamu.company.user.service;

import shamu.company.user.dto.UserContactInformationDTO;
import shamu.company.user.entity.UserContactInformation;

public interface UserContactInformationService {
    UserContactInformationDTO update(UserContactInformation userContactInformation);
}
