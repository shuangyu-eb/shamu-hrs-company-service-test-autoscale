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
import shamu.company.account.service.AccountService;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.common.multitenant.TenantContext;
import shamu.company.common.service.TenantService;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.user.dto.CreatePasswordDto;
import shamu.company.user.dto.UserLoginDto;
import shamu.company.user.service.UserService;
import shamu.company.utils.Base64Utils;

@RestApiController
public class AccountRestController {

  private final UserService userService;

  private final Auth0Helper auth0Helper;

  private final AccountService accountService;

  private final TenantService tenantService;

  @Autowired
  public AccountRestController(
      final UserService userService,
      final Auth0Helper auth0Helper,
      final AccountService accountService,
      final TenantService tenantService) {
    this.userService = userService;
    this.auth0Helper = auth0Helper;
    this.accountService = accountService;
    this.tenantService = tenantService;
  }

  @GetMapping("account/password/{token}/{companyId}")
  public Boolean createPasswordTokenExist(
      @PathVariable final String token, @PathVariable final String companyId) {
    final String decodeCompanyId = Base64Utils.decodeCompanyId(companyId);
    return isCompanyExists(decodeCompanyId) && userService.createPasswordTokenExist(token);
  }

  private boolean isCompanyExists(final String companyId) {
    final boolean isExists = tenantService.isCompanyExists(companyId);
    if (!isExists) {
      return false;
    }
    TenantContext.setCurrentTenant(companyId);
    return true;
  }

  @PatchMapping("account/password")
  public HttpEntity<String> createPassword(
      @RequestBody @Valid final CreatePasswordDto createPasswordDto) {
    final String companyId = createPasswordDto.getCompanyId();
    final boolean isExists = tenantService.isCompanyExists(companyId);
    if (!isExists) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    TenantContext.setCurrentTenant(createPasswordDto.getCompanyId());
    accountService.createPassword(createPasswordDto);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("account/password/{passwordToken}/{invitationToken}/{companyId}")
  public boolean createPasswordAndInvitationTokenExist(
      @PathVariable("passwordToken") final String passwordToken,
      @PathVariable("invitationToken") final String invitationToken,
      @PathVariable final String companyId) {
    final String decodeCompanyId = Base64Utils.decodeCompanyId(companyId);
    return isCompanyExists(decodeCompanyId)
        && accountService.createPasswordAndInvitationTokenExist(passwordToken, invitationToken);
  }

  @PatchMapping("account/unlock")
  public HttpEntity<String> unlock(@RequestBody @Valid final UserLoginDto userLoginDto) {
    auth0Helper.login(userLoginDto.getEmailWork(), userLoginDto.getPassword());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PatchMapping("account/change-work-email/{token}/{companyId}")
  public boolean validateChangeWorkEmail(
      @PathVariable final String token, @PathVariable final String companyId) {
    final String decodeCompanyId = Base64Utils.decodeCompanyId(companyId);
    return isCompanyExists(decodeCompanyId) && userService.changeWorkEmailTokenExist(token);
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
