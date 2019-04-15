package shamu.company.user.controller;

import shamu.company.common.config.annotations.RestApiController;
import shamu.company.company.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shamu.company.user.dto.PersonalInformationDTO;
import shamu.company.user.service.UserService;

@RestApiController
public class UserRestController {
    @Autowired
    UserService userService;

    @Autowired
    CompanyService companyService;

    @PostMapping(value = "user/sign-up/email")
    public HttpEntity sendVerifyEmail(@RequestBody String email) {
        userService.sendVerifyEmail(email);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping(value = "user/verify")
    public HttpEntity finishUserVerification(@RequestBody String token) {
        userService.finishUserVerification(token);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping(value = "user/check/email/{email}")
    public Boolean checkEmail(@PathVariable String email) {
        return userService.existsByEmailWork(email);
    }

    @GetMapping(value = "user/check/company-name/{companyName}")
    public Boolean checkCompanyName(@PathVariable String companyName) {
        return companyService.existsByName(companyName);
    }

    @GetMapping(value = "user/check/desired-url/{desiredUrl}")
    public Boolean checkDesiredUrl(@PathVariable String desiredUrl) {
        return companyService.existsBySubdomainName(desiredUrl);
    }

    @GetMapping("user/{userId}/personal-information")
    public PersonalInformationDTO getPersonalInformation(@PathVariable Long userId){
        PersonalInformationDTO personalInformationDTO = userService.getPersonalInformation(userId);
        return personalInformationDTO;
    }
}
