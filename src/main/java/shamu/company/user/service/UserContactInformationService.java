package shamu.company.user.service;

import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.entity.UserContactInformation;

public interface UserContactInformationService {
  UserContactInformationDto update(UserContactInformation userContactInformation);
}
