package shamu.company.user.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.validation.constraints.FileValidate;
import shamu.company.employee.dto.EmailUpdateDto;
import shamu.company.user.dto.AccountInfoDto;
import shamu.company.user.dto.ChangePasswordDto;
import shamu.company.user.dto.CurrentUserDto;
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

@RestApiController
@Validated
public class UserRestController extends BaseRestController {

  private final UserService userService;

  private final UserMapper userMapper;

  public UserRestController(final UserService userService, final UserMapper userMapper) {
    this.userService = userService;
    this.userMapper = userMapper;
  }

  @PostMapping(value = "users")
  public HttpEntity signUp(@RequestBody final UserSignUpDto signUpDto) {
    userService.signUp(signUpDto);
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
    userService.sendResetPasswordEmail(email);
    return new ResponseEntity(HttpStatus.OK);
  }

  @PatchMapping("users/password-reset")
  public boolean resetPassword(@RequestBody @Valid final UpdatePasswordDto updatePasswordDto) {
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
    final List<User> users = userService.findAllUsersByCompany(findCompanyId());
    return userMapper.convertToUserDtos(users);
  }

  @GetMapping("current/user-info")
  public CurrentUserDto getUserInfo(final HttpServletRequest request) {
    final String mockId = request.getHeader("X-Mock-To");
    if (Strings.isBlank(mockId)) {
      final CurrentUserDto userDto =
          userService.getCurrentUserInfo(findAuthentication().getUserId());
      userService.cacheUser(findToken(), userDto.getId());
      return userDto;
    }

    final User user = userService.findActiveUserById(findAuthentication().getUserId());
    final Role role = user.getRole();
    if (role != Role.SUPER_ADMIN) {
      throw new ForbiddenException("You are not super admin!");
    }

    userService.cacheUser(findToken(), mockId);
    return userService.getMockUserInfo(mockId);
  }

  @GetMapping("current/company-name")
  public String getCompanyName(final HttpServletRequest request) {
    final User user = userService.findById(findUserId());
    return user.getCompany().getName();
  }

  @GetMapping("/users/check-password/{password}")
  @PreAuthorize("hasAuthority('EDIT_SELF')")
  public void checkPassword(@PathVariable final String password) {
    userService.checkPassword(findAuthUser().getEmail(), password);
  }

  @PatchMapping("/users/work-email")
  @PreAuthorize("hasAuthority('EDIT_SELF')")
  public HttpEntity updateWorkEmail(@RequestBody @Valid final EmailUpdateDto emailUpdateDto) {
    userService.updateWorkEmail(emailUpdateDto);
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
}
