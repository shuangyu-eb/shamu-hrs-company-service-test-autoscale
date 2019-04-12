package shamu.company.user.controller;

import shamu.company.common.config.annotations.RestApiController;
import shamu.company.user.dto.UserContactInformationDTO;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.service.UserContactInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestApiController
public class UserContactInformationController {

    @Autowired
    UserContactInformationService contactInformationService;

    @PatchMapping("user-contact-info")
    public UserContactInformationDTO update(@RequestBody UserContactInformation userContactInformation){
        return contactInformationService.update(userContactInformation);
    }
}
