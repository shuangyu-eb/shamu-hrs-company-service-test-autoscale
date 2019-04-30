package shamu.company.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.UserPersonalInformationService;

@RestApiController
public class UserPersonalInformationRestController extends BaseRestController {

  private final UserPersonalInformationService userPersonalInformationService;

  @Autowired
  public UserPersonalInformationRestController(
      UserPersonalInformationService userPersonalInformationService) {
    this.userPersonalInformationService = userPersonalInformationService;
  }

  @PatchMapping("user-personal-information")
  @PreAuthorize("hasPermission(#userPersonalInformationDto.id,'PERSONAL_INFORMATION', 'EDIT_USER')"
      + " or hasPermission(#userPersonalInformationDto.id,'PERSONAL_INFORMATION', 'EDIT_SELF')")
  public UserPersonalInformationDto update(
      @RequestBody UserPersonalInformationDto userPersonalInformationDto) {
    UserPersonalInformation userPersonalInformation =
        userPersonalInformationDto.getUserPersonalInformation();
    UserPersonalInformation userPersonalInformationUpdated =
        userPersonalInformationService.update(userPersonalInformation);
    return new UserPersonalInformationDto(userPersonalInformationUpdated);
  }
}
