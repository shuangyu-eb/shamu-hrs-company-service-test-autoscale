package shamu.company.user.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.user.service.UserBenefitsSettingService;

@RestApiController
public class UserBenefitsSettingRestController extends BaseRestController {

  private final UserBenefitsSettingService benefitService;

  public UserBenefitsSettingRestController(final UserBenefitsSettingService benefitService) {
    this.benefitService = benefitService;
  }

  @GetMapping("benefits-setting/hidden-banner")
  public Boolean findUserBenefitsHiddenBanner() {
    return benefitService.findUserBenefitsHiddenBanner(findUserId());
  }

  @PatchMapping("benefits-setting/hidden-banner")
  public HttpEntity updateUserBenefitsHiddenBanner() {
    benefitService.updateUserBenefitsHiddenBanner(findUserId());
    return new ResponseEntity(HttpStatus.OK);
  }
}
