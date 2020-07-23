package shamu.company.account.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.validation.Valid;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.account.service.AccountService;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.common.multitenant.TenantContext;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.user.dto.CreatePasswordDto;
import shamu.company.user.dto.UserLoginDto;
import shamu.company.user.service.UserService;

@RestApiController
public class AccountRestController {

  private final UserService userService;

  private final Auth0Helper auth0Helper;

  private final AccountService accountService;

  @Autowired
  public AccountRestController(
      final UserService userService,
      final Auth0Helper auth0Helper,
      final AccountService accountService) {
    this.userService = userService;
    this.auth0Helper = auth0Helper;
    this.accountService = accountService;
  }

  @GetMapping("account/password/{token}")
  public Boolean createPasswordTokenExist(@PathVariable final String token) {
    return userService.createPasswordTokenExist(token);
  }

  @PatchMapping("account/password")
  public HttpEntity<String> createPassword(
      @RequestBody @Valid final CreatePasswordDto createPasswordDto) {
    TenantContext.setCurrentTenant(createPasswordDto.getCompanyId());
    accountService.createPassword(createPasswordDto);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("account/password/{passwordToken}/{invitationToken}")
  public boolean createPasswordAndInvitationTokenExist(
      @PathVariable("passwordToken") final String passwordToken,
      @PathVariable("invitationToken") final String invitationToken,
      @PathVariable final String companyId) {
    final String decodedCompanyId =
        StringUtils.reverse(
            new String(Base64.getDecoder().decode(companyId), StandardCharsets.UTF_8));
    TenantContext.setCurrentTenant(decodedCompanyId);
    return accountService.createPasswordAndInvitationTokenExist(passwordToken, invitationToken);
  }

  @PatchMapping("account/unlock")
  public HttpEntity<String> unlock(@RequestBody @Valid final UserLoginDto userLoginDto) {
    auth0Helper.login(userLoginDto.getEmailWork(), userLoginDto.getPassword());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PatchMapping("account/change-work-email/{token}")
  public boolean validateChangeWorkEmail(@PathVariable final String token) {
    return userService.changeWorkEmailTokenExist(token);
  }

  @PostMapping("account/{email}/verification-email")
  public HttpEntity<String> resendVerificationEmail(@PathVariable final String email) {
    userService.resendVerificationEmail(email);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("account/email/{email}")
  public HttpEntity<String> findByEmailWork(@PathVariable final String email) {
    if (userService.checkUserVerifiedEmail(email)) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
