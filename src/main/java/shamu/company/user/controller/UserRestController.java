package shamu.company.user.controller;

import com.auth0.json.auth.CreatedUser;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.common.database.LiquibaseManager;
import shamu.company.common.entity.Tenant;
import shamu.company.common.exception.errormapping.AlreadyExistsException;
import shamu.company.common.exception.errormapping.ForbiddenException;
import shamu.company.common.multitenant.TenantContext;
import shamu.company.common.service.TenantService;
import shamu.company.common.validation.constraints.FileValidate;
import shamu.company.company.service.CompanyService;
import shamu.company.email.service.EmailService;
import shamu.company.employee.dto.EmailUpdateDto;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.helpers.auth0.exception.SignUpFailedException;
import shamu.company.user.dto.AccountInfoDto;
import shamu.company.user.dto.ChangePasswordDto;
import shamu.company.user.dto.CurrentUserDto;
import shamu.company.user.dto.IndeedUserDto;
import shamu.company.user.dto.UpdatePasswordDto;
import shamu.company.user.dto.UserAvatarDto;
import shamu.company.user.dto.UserDto;
import shamu.company.user.dto.UserRoleAndStatusInfoDto;
import shamu.company.user.dto.UserRoleUpdateDto;
import shamu.company.user.dto.UserSignUpDto;
import shamu.company.user.dto.UserStatusUpdateDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.service.UserService;
import shamu.company.utils.Base64Utils;

@RestApiController
@Validated
public class UserRestController extends BaseRestController {

  private final UserService userService;

  private final EmailService emailService;

  private final CompanyService companyService;

  private final TenantService tenantService;

  private final UserMapper userMapper;

  private final Auth0Helper auth0Helper;

  private final LiquibaseManager liquibaseManager;

  private static final String MOCK_USER_HEADER = "X-Mock-To";

  public UserRestController(
      final UserService userService,
      final EmailService emailService,
      final UserMapper userMapper,
      final CompanyService companyService,
      final TenantService tenantService,
      final Auth0Helper auth0Helper,
      final LiquibaseManager liquibaseManager) {
    this.userService = userService;
    this.emailService = emailService;
    this.userMapper = userMapper;
    this.companyService = companyService;
    this.tenantService = tenantService;
    this.auth0Helper = auth0Helper;
    this.liquibaseManager = liquibaseManager;
  }

  @PostMapping(value = "users")
  public HttpEntity signUp(@RequestBody final UserSignUpDto signUpDto) {
    if (tenantService.isCompanyExists(signUpDto.getCompanyName())) {
      throw new AlreadyExistsException("Company name already exists.", "company name");
    }

    final CreatedUser user = auth0Helper.signUp(signUpDto.getWorkEmail(), signUpDto.getPassword());
    final com.auth0.json.mgmt.users.User auth0User;
    try {
      auth0User = auth0Helper.updateAuthUserAppMetaData(user.getUserId());
    } catch (final Exception e) {
      auth0Helper.deleteUser(user.getUserId());
      throw new SignUpFailedException("Auth0 account update failed.", e);
    }
    final Map<String, Object> appMetaData = auth0User.getAppMetadata();
    final String companyId = (String) appMetaData.get(Auth0Helper.COMPANY_ID);
    final String userId = (String) appMetaData.get(Auth0Helper.USER_ID);
    try {
      liquibaseManager.addSchema(companyId, signUpDto.getCompanyName());
      TenantContext.withInTenant(companyId, () -> userService.signUp(signUpDto, userId));
    } catch (final Exception e) {
      auth0Helper.deleteUser(auth0User.getId());
      throw new SignUpFailedException("Sign up failed.", e);
    }
    return new ResponseEntity(HttpStatus.OK);
  }

  @PostMapping("/users/indeed-verification-email")
  public HttpStatus sendVerificationEmailForIndeedUser(
      @RequestBody final IndeedUserDto indeedUserDto) {
    final com.auth0.json.mgmt.users.User auth0User;
    try {
      auth0User = auth0Helper.updateAuthUserAppMetaData(indeedUserDto.getId());
    } catch (final Exception e) {
      throw new SignUpFailedException("Auth0 account update failed.", e);
    }
    final Map<String, Object> appMetaData = auth0User.getAppMetadata();
    final String companyId = (String) appMetaData.get(Auth0Helper.COMPANY_ID);
    final String userId = (String) appMetaData.get(Auth0Helper.USER_ID);
    try {
      liquibaseManager.addSchema(companyId, indeedUserDto.getCompanyName());
      TenantContext.withInTenant(
          companyId, () -> emailService.sendVerificationEmail(indeedUserDto.getEmail(), userId));
    } catch (final Exception e) {
      auth0Helper.deleteUser(auth0User.getId());
      throw new SignUpFailedException("Sign up failed.", e);
    }

    return HttpStatus.OK;
  }

  @PostMapping(value = "indeed-users/{companyId}")
  public HttpEntity indeedSignUp(
      @RequestBody final IndeedUserDto indeedUserDto, @PathVariable final String companyId) {

    final String userId = indeedUserDto.getId();
    final UserSignUpDto signUpDto =
        UserSignUpDto.builder()
            .firstName(indeedUserDto.getFirstName())
            .lastName(indeedUserDto.getLastName())
            .workEmail(indeedUserDto.getEmail())
            .build();
    TenantContext.withInTenant(companyId, () -> userService.signUp(signUpDto, userId));
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping(value = "users/email-check/{email}")
  public Boolean checkEmail(@PathVariable final String email) {
    return userService.existsByEmailWork(email);
  }

  @PostMapping("users/{id}/head-portrait")
  @PreAuthorize(
      "hasPermission(#id,'USER', 'EDIT_USER')" + " or hasPermission(#id,'USER', 'EDIT_SELF')")
  public String handleFileUpload(
      @PathVariable final String id,
      @RequestParam("file")
          @FileValidate(
              maxSize = 2 * 1024 * 1024,
              fileFormat = {"JPEG", "PNG", "GIF"})
          final MultipartFile file) {
    return userService.handleUploadFile(id, file);
  }

  @DeleteMapping("users/{id}/head-portrait")
  @PreAuthorize(
      "hasPermission(#id,'USER', 'EDIT_USER')" + " or hasPermission(#id,'USER', 'EDIT_SELF')")
  public HttpEntity deleteHeadPortrait(@PathVariable final String id) {
    userService.handleDeleteHeadPortrait(id);
    return new ResponseEntity(HttpStatus.OK);
  }

  @PostMapping(value = "users/password-reset/{email}")
  public HttpEntity sendResetPasswordEmail(@PathVariable final String email) {
    final Tenant tenant = tenantService.findTenantByUserEmailWork(email);
    TenantContext.setCurrentTenant(tenant.getCompanyId());
    userService.sendResetPasswordEmail(email);
    return new ResponseEntity(HttpStatus.OK);
  }

  @PatchMapping("users/password-reset")
  public boolean resetPassword(@RequestBody @Valid final UpdatePasswordDto updatePasswordDto) {
    TenantContext.setCurrentTenant(Base64Utils.decodeCompanyId(updatePasswordDto.getCompanyId()));
    userService.resetPassword(updatePasswordDto);
    return true;
  }

  @PatchMapping("users/password")
  public HttpEntity updatePassword(@RequestBody @Valid final ChangePasswordDto changePasswordDto) {
    userService.updatePassword(changePasswordDto, findUserId());
    return new ResponseEntity(HttpStatus.OK);
  }

  @PreAuthorize("hasPermission(#id,'USER', 'EDIT_SELF')")
  @GetMapping("users/{id}/account-info")
  public AccountInfoDto getPreSetAccountInfo(@PathVariable final String id) {
    return userService.getPreSetAccountInfoByUserId(id);
  }

  @PreAuthorize("hasPermission(#id,'USER', 'EDIT_SELF')")
  @GetMapping("users/{id}/avatar")
  public UserAvatarDto getUserAvatar(@PathVariable final String id) {
    final User user = userService.findById(id);
    return userMapper.convertToUserAvatarDto(user);
  }

  @PatchMapping("users/{id}/user-role")
  @PreAuthorize("hasPermission(#id, 'USER', 'VIEW_SETTING')")
  public UserRoleAndStatusInfoDto updateUserRole(
      @PathVariable final String id, @RequestBody final UserRoleUpdateDto userRoleUpdateDto) {
    User user = userService.findById(id);
    user = userService.updateUserRole(findAuthUser().getEmail(), userRoleUpdateDto, user);
    return userMapper.convertToUserRoleAndStatusInfoDto(user);
  }

  @PatchMapping("users/{id}/deactivate")
  @PreAuthorize("hasPermission(#id, 'USER', 'DEACTIVATE_USER')")
  public UserRoleAndStatusInfoDto deactivateUser(
      @PathVariable final String id, @RequestBody final UserStatusUpdateDto userStatusUpdateDto) {
    User user = userService.findById(id);
    user = userService.deactivateUser(findAuthUser().getEmail(), userStatusUpdateDto, user);
    return userMapper.convertToUserRoleAndStatusInfoDto(user);
  }

  @DeleteMapping("users/{id}")
  @PreAuthorize("hasPermission(#id, 'USER', 'EDIT_USER')")
  public HttpEntity deleteUser(@PathVariable final String id) {
    final User employee = userService.findById(id);
    userService.deleteUser(employee);
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping("users")
  public List<UserDto> getAllUsers() {
    final List<User> users = userService.findAllUsersByCompany();
    return userMapper.convertToUserDtos(users);
  }

  @GetMapping("current/user-info")
  public CurrentUserDto getUserInfo(final HttpServletRequest request) {
    final String mockId = request.getHeader(MOCK_USER_HEADER);
    if (Strings.isBlank(mockId)) {
      final CurrentUserDto userDto =
          userService.getCurrentUserInfo(
              findAuthentication().getUserId(), findAuthentication().getUserEmail());
      userService.cacheUser(findToken(), userDto.getId());
      return userDto;
    }
    handleSuperAdminCacheProcess(mockId);
    return userService.getMockUserInfo(mockId);
  }

  private void handleSuperAdminCacheProcess(final String mockUserId) {
    final String currentUserId = findAuthentication().getUserId();
    final Role role = auth0Helper.getUserRoleByUserId(currentUserId);
    if (role != Role.SUPER_ADMIN) {
      throw new ForbiddenException(
          String.format("User with id %s is not super admin.", currentUserId));
    }

    userService.cacheUser(findToken(), mockUserId);
  }

  @GetMapping("current/company-name")
  public String getCompanyName() {
    return companyService.getCompany().getName();
  }

  @GetMapping("{userId}/check-personal-info-complete")
  public Boolean checkPersonalInfoComplete(@PathVariable final String userId) {
    return userService.checkPersonalInfoComplete(userId);
  }

  @GetMapping("/users/check-password/{password}")
  @PreAuthorize("hasAuthority('EDIT_SELF')")
  public void checkPassword(@PathVariable final String password) {
    userService.checkPassword(findAuthUser().getEmail(), password);
  }

  @PatchMapping("/users/work-email")
  @PreAuthorize(
      "hasPermission(#emailUpdateDto.userId, 'USER', 'EDIT_SELF')"
          + "or hasPermission(#emailUpdateDto.userId, 'USER', 'EDIT_USER')")
  public HttpEntity updateWorkEmail(@RequestBody @Valid final EmailUpdateDto emailUpdateDto) {
    userService.updateWorkEmail(emailUpdateDto, findAuthUser().getEmail());
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping("/users/change-work-email")
  @PreAuthorize("hasAuthority('EDIT_SELF')")
  public String getChangeWorkEmail() {
    final User user = userService.findById(findAuthUser().getId());
    return user.getChangeWorkEmail();
  }

  @GetMapping("/users/send-verify-work-email")
  @PreAuthorize("hasAuthority('EDIT_SELF')")
  public void sendVerifyChangeWorkEmail() {
    final User user = userService.findById(findAuthUser().getId());
    userService.sendVerifyChangeWorkEmail(user);
  }

  @GetMapping("/users/current-active-announcement/is-dismissed/{id}")
  public Boolean isCurrentActiveAnnouncementDismissed(@PathVariable final String id) {
    return userService.isCurrentActiveAnnouncementDismissed(findAuthUser().getId(), id);
  }

  @PostMapping("/users/dismiss-current-active-announcement/{id}")
  public HttpEntity dismissCurrentActiveAnnouncement(@PathVariable final String id) {
    userService.dismissCurrentActiveAnnouncement(findAuthUser().getId(), id);
    return new HttpEntity(HttpStatus.OK);
  }

  @GetMapping("users/registered")
  public List<UserDto> getAllRegisteredUsers() {
    final List<User> users = userService.findRegisteredUsers();
    return userMapper.convertToUserDtos(users);
  }

  @PatchMapping("current/cache/{id}")
  public HttpEntity cacheTokenAndAuthUser(
      @PathVariable final String id, final HttpServletRequest request) {
    final String mockId = request.getHeader(MOCK_USER_HEADER);
    if (StringUtils.isBlank(mockId)) {
      userService.cacheUser(findToken(), id.toUpperCase());
    } else {
      handleSuperAdminCacheProcess(mockId);
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("users/is-user-invitation-capability-frozen")
  @PreAuthorize("hasAuthority('CREATE_USER')")
  public Boolean isUserInvitationAbilityFrozen() {
    return userService.isUserInvitationCapabilityFrozen(findUserId());
  }
}
