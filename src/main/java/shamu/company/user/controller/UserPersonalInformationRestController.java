package shamu.company.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.UserPersonalInformationService;

@RestApiController
public class UserPersonalInformationRestController {

  @Autowired UserPersonalInformationService userPersonalInformationService;

  @PatchMapping("user-personal-information")
  public UserPersonalInformationDto update(
      @RequestBody UserPersonalInformationDto userPersonalInformationDto) {
    UserPersonalInformation userPersonalInformation =
        userPersonalInformationDto.getUserPersonalInformation();
    UserPersonalInformation userPersonalInformationUpdated =
        userPersonalInformationService.update(userPersonalInformation);
    UserPersonalInformationDto userPersonalInformationDtoUpdated =
        new UserPersonalInformationDto(userPersonalInformationUpdated);
    return userPersonalInformationDtoUpdated;
  }
}
