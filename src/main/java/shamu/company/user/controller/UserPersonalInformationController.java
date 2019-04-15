package shamu.company.user.controller;

import shamu.company.common.config.annotations.RestApiController;
import shamu.company.user.dto.UserPersonalInformationDTO;
import shamu.company.user.service.UserPersonalInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestApiController
public class UserPersonalInformationController {
    @Autowired
    UserPersonalInformationService userPersonalInformationService;

    @PatchMapping("user-personal-information")
    public UserPersonalInformationDTO update(@RequestBody UserPersonalInformationDTO userPersonalInformationDTO) {
         return userPersonalInformationService.update(userPersonalInformationDTO);
    }

}
