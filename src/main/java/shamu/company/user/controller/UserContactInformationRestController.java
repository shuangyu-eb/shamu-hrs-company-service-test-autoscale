package shamu.company.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.service.UserContactInformationService;

@RestApiController
public class UserContactInformationRestController {

  @Autowired
  UserContactInformationService contactInformationService;

  @PatchMapping("user-contact-information")
  public UserContactInformationDto update(
      @RequestBody UserContactInformation userContactInformation) {
    return contactInformationService.update(userContactInformation);
  }
}
