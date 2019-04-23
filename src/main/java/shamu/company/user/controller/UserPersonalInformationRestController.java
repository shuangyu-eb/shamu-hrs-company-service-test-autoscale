package shamu.company.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.authorization.Permission.Name;
import shamu.company.authorization.Type;
import shamu.company.authorization.annotation.HasAnyPermission;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.UserPersonalInformationService;

@RestApiController
public class UserPersonalInformationRestController extends BaseRestController {

  @Autowired
  UserPersonalInformationService userPersonalInformationService;

  @PatchMapping("user-personal-information")
  @HasAnyPermission(targetId = "#userPersonalInformationDto.id",
      targetType = Type.PERSONAL_INFORMATION, permissions = {Name.EDIT_USER, Name.EDIT_SELF})
  public UserPersonalInformationDto update(
      @RequestBody UserPersonalInformationDto userPersonalInformationDto) {
    UserPersonalInformation userPersonalInformation =
        userPersonalInformationDto.getUserPersonalInformation();
    UserPersonalInformation userPersonalInformationUpdated =
        userPersonalInformationService.update(userPersonalInformation);
    return new UserPersonalInformationDto(userPersonalInformationUpdated);
  }
}
