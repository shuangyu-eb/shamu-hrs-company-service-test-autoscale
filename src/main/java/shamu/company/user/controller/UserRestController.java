package shamu.company.user.controller;

import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.company.CompanyService;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.dto.AccountInfoDto;
import shamu.company.user.dto.CurrentUserDto;
import shamu.company.user.dto.UpdatePasswordDto;
import shamu.company.user.dto.UserAvatarDto;
import shamu.company.user.dto.UserDto;
import shamu.company.user.dto.UserRoleAndStatusInfoDto;
import shamu.company.user.dto.UserSignUpDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.pojo.UserRoleUpdatePojo;
import shamu.company.user.pojo.UserStatusUpdatePojo;
import shamu.company.user.service.UserService;
import shamu.company.utils.AwsUtil;
import shamu.company.utils.AwsUtil.Type;

@RestApiController
public class UserRestController extends BaseRestController {

  private final UserService userService;

  private final CompanyService companyService;

  private final AwsUtil awsUtil;

  private final UserMapper userMapper;

  public UserRestController(final UserService userService, final CompanyService companyService,
      final AwsUtil awsUtil, final UserMapper userMapper) {
    this.userService = userService;
    this.companyService = companyService;
    this.awsUtil = awsUtil;
    this.userMapper = userMapper;
  }

  @PostMapping(value = "user/sign-up")
  public HttpEntity signUp(@RequestBody final UserSignUpDto signUpDto) {
    userService.signUp(signUpDto);
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping(value = "user/check/company-name/{companyName}")
  public Boolean checkCompanyName(@PathVariable final String companyName) {
    return companyService.existsByName(companyName);
  }

  @GetMapping(value = "user/check/email/{email}")
  public Boolean checkEmail(@PathVariable final String email) {
    return userService.existsByEmailWork(email);
  }

  @GetMapping(value = "users/{id}/head-portrait")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_USER_PERSONAL')")
  public String getHeadPortrait(@PathVariable @HashidsFormat final Long id) {
    final User user = getUser();

    if (user.getId().equals(id)
        || user.getRole() == Role.ADMIN) {
      return userService.getHeadPortrait(id);
    }

    return null;
  }

  @PostMapping("users/{id}/head-portrait")
  @PreAuthorize(
      "hasPermission(#id,'USER', 'EDIT_USER')"
          + " or hasPermission(#id,'USER', 'EDIT_SELF')")
  public String handleFileUpload(
      @PathVariable @HashidsFormat final Long id, @RequestParam("file") final MultipartFile file)
      throws IOException {
    final String path = awsUtil.uploadFile(file, Type.IMAGE);

    if (Strings.isBlank(path)) {
      return null;
    }

    final User user = userService.findUserById(id);
    final String originalPath = user.getImageUrl();
    if (originalPath != null) {
      awsUtil.deleteFile(originalPath);
    }

    user.setImageUrl(path);
    userService.save(user);

    return path;
  }

  @PostMapping(value = "user/password/reset/email")
  public HttpEntity sendResetPasswordEmail(@RequestBody final String email) {
    userService.sendResetPasswordEmail(email);
    return new ResponseEntity(HttpStatus.OK);
  }

  @PatchMapping("user/password/reset/token")
  public boolean resetPassword(@RequestBody final UpdatePasswordDto updatePasswordDto) {
    userService.resetPassword(updatePasswordDto);
    return true;
  }

  @PreAuthorize("hasPermission(#id,'USER', 'EDIT_SELF')")
  @GetMapping("users/{id}/account-info")
  public AccountInfoDto getPreSetAccountInfo(@PathVariable @HashidsFormat final Long id) {
    return userService.getPreSetAccountInfoByUserId(id);
  }

  @GetMapping("users/{id}/avatar")
  public UserAvatarDto getUserAvatar(@PathVariable @HashidsFormat final Long id) {
    final User user = userService.findUserById(id);
    return userMapper.convertToUserAvatarDto(user);
  }

  @PatchMapping("users/{id}/user-role")
  @PreAuthorize("hasPermission(#id, 'USER', 'VIEW_SETTING')")
  public UserRoleAndStatusInfoDto updateUserRole(@PathVariable @HashidsFormat final Long id,
      @RequestBody final UserRoleUpdatePojo userRoleUpdatePojo) {
    final User currentUser = getUser();
    final User user = userService.findUserById(id);
    return userMapper.convertToUserRoleAndStatusInfoDto(userService
        .updateUserRole(currentUser, userRoleUpdatePojo, user));
  }

  @PatchMapping("users/{id}/user-status")
  @PreAuthorize("hasPermission(#id, 'USER', 'VIEW_SETTING')")
  public UserRoleAndStatusInfoDto updateUserStatus(@PathVariable @HashidsFormat final Long id,
      @RequestBody final UserStatusUpdatePojo userStatusUpdatePojo) {
    final User currentUser = getUser();
    final User user = userService.findUserById(id);
    return userMapper.convertToUserRoleAndStatusInfoDto(userService
        .updateUserStatus(currentUser, userStatusUpdatePojo, user));
  }

  @GetMapping("users/all")
  public List<UserDto> getAllUsers() {
    final List<User> users = userService.findAllUsersByCompany(getCompany());
    return userMapper.convertToUserDtos(users);
  }

  @GetMapping("current/user-info")
  public CurrentUserDto getUserInfo() {
    return userService.getCurrentUserInfo(getUserId());
  }

  @GetMapping("has-privilege/user/{userId}")
  public boolean hasUserPermission(@HashidsFormat @PathVariable final Long userId) {
    return userService.hasUserAccess(getUser(), userId);
  }
}
