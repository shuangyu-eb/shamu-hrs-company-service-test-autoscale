package shamu.company.account.controller;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.user.dto.CreatePasswordDto;
import shamu.company.user.dto.UserLoginDto;
import shamu.company.user.service.UserService;

@RestApiController
public class AccountRestController {

  private final UserService userService;

  private final Auth0Helper auth0Helper;

  @Autowired
  public AccountRestController(final UserService userService, final Auth0Helper auth0Helper) {
    this.userService = userService;
    this.auth0Helper = auth0Helper;
  }

  @GetMapping("account/password/{token}")
  public Boolean createPasswordTokenExist(@PathVariable final String token) {
    return userService.createPasswordTokenExist(token);
  }

  @PatchMapping("account/password")
  public HttpEntity createPassword(@RequestBody @Valid final CreatePasswordDto createPasswordDto) {
    userService.createPassword(createPasswordDto);
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping("account/password/{passwordToken}/{invitationToken}")
  public boolean createPasswordAndInvitationTokenExist(
      @PathVariable("passwordToken") final String passwordToken,
      @PathVariable("invitationToken") final String invitationToken) {
    return userService.createPasswordAndInvitationTokenExist(passwordToken, invitationToken);
  }

  @PatchMapping("account/unlock")
  public HttpEntity unlock(@RequestBody @Valid final UserLoginDto userLoginDto) {
    auth0Helper.login(userLoginDto.getEmailWork(), userLoginDto.getPassword());
    return new ResponseEntity(HttpStatus.OK);
  }

  @PatchMapping("account/change-work-email/{token}")
  public boolean validateChangeWorkEmail(@PathVariable final String token) {
    return userService.changeWorkEmailTokenExist(token);
  }

  @PostMapping("account/{email}/verification-email")
  public HttpEntity resendVerificationEmail(@PathVariable final String email) {
    userService.resendVerificationEmail(email);
    return new ResponseEntity(HttpStatus.NO_CONTENT);
  }
}
