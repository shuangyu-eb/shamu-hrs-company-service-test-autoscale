package shamu.company.user.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.user.service.UserBenefitsSettingService;

@RestApiController
public class UserBenefitsSettingRestController extends BaseRestController {

  private final UserBenefitsSettingService benefitService;

  public UserBenefitsSettingRestController(final UserBenefitsSettingService benefitService) {
    this.benefitService = benefitService;
  }

  @GetMapping("benefits-setting/effect-year/{year}")
  public Boolean findUserBenefitsEffectYear(@PathVariable final String year) {
    return benefitService.findUserBenefitsEffectYear(findUserId(), year);
  }

  @PatchMapping("benefits-setting/effect-year")
  public HttpEntity saveUserBenefitsSettingEffectYear(@RequestBody final String effectYear) {
    benefitService.saveUserBenefitsSettingEffectYear(findUserId(), effectYear);
    return new ResponseEntity(HttpStatus.OK);
  }
}
