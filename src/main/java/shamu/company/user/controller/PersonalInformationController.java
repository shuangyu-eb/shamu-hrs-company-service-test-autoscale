package shamu.company.user.controller;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.user.dto.BasicInformationDTO;
import shamu.company.user.dto.PersonalInformationDTO;
import shamu.company.user.service.PersonalInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestApiController
public class PersonalInformationController {
    @Autowired
    PersonalInformationService personalInformationService;

    @GetMapping("user/personal-information/{userId}")
    public PersonalInformationDTO getPersonalInformation(@PathVariable Long userId){
        PersonalInformationDTO personalInformationDTO = personalInformationService.getPersonalInformation(userId);
        return personalInformationDTO;
    }

    @PatchMapping("user/personal-information/basic-information")
    public BasicInformationDTO updateBasicInformation(@RequestBody BasicInformationDTO basicInformationDTO){
        BasicInformationDTO basicInformationDTOUpdated = personalInformationService.updateBasicInformation(basicInformationDTO);
        return basicInformationDTOUpdated;
    }
}
