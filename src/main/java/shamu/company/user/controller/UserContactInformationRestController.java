package shamu.company.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.service.UserContactInformationService;

@RestApiController
public class UserContactInformationRestController extends BaseRestController {

  @Autowired
  UserContactInformationService contactInformationService;

  @PatchMapping("user-contact-information")
  public UserContactInformationDto update(
      @RequestBody UserContactInformationDto userContactInformationDto) {
    UserContactInformation userContactInformation =
        userContactInformationDto.getUserContactInformation();
    UserContactInformation userContactInformationUpdated =
        contactInformationService.update(userContactInformation);
    return new UserContactInformationDto(userContactInformationUpdated);
  }
}
