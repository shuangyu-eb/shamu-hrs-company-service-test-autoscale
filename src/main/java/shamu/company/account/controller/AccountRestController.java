package shamu.company.account.controller;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.user.dto.UpdatePasswordDto;
import shamu.company.user.dto.UserLoginDto;
import shamu.company.user.service.UserService;

@RestApiController
public class AccountRestController {

  private final UserService userService;

  @Autowired
  public AccountRestController(final UserService userService) {
    this.userService = userService;
  }

  @GetMapping("account/password/{token}")
  public Boolean createPasswordTokenExist(@PathVariable final String token) {
    return userService.createPasswordTokenExist(token);
  }

  @PatchMapping("account/password")
  public HttpEntity createPassword(@RequestBody final UpdatePasswordDto updatePasswordDto) {
    userService.createPassword(updatePasswordDto);
    return new ResponseEntity(HttpStatus.OK);
  }

  @PatchMapping("account/unlock")
  public HttpEntity unlock(@RequestBody @Valid final UserLoginDto userLoginDto) {
    userService.unlock(userLoginDto);
    return new ResponseEntity(HttpStatus.OK);
  }
}
