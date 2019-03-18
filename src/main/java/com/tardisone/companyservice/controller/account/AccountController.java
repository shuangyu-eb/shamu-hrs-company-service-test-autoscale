package com.tardisone.companyservice.controller.account;

import com.tardisone.companyservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AccountController {

  @Autowired
  UserService userService;

  @GetMapping(value = {"/account/password/reset/key/{resetPasswordToken}"})
  public String toResetPassword(@PathVariable("resetPasswordToken") String resetPasswordToken) {
    boolean isExist = userService.isExistByResetPasswordToken(resetPasswordToken);
    if (isExist) {
      return "account";
    }
    return "error/404";
  }

  @GetMapping("/account/password/reset/done")
  public String done() {
    return "account";
  }

  @GetMapping("/account/password/reset")
  public String resetPassword() {
    return "account";
  }

}
