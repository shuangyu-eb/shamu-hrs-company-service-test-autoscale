package shamu.company.user.controller;

import java.io.IOException;
import java.util.List;
import javax.validation.Valid;
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
import shamu.company.user.pojo.ChangePasswordPojo;
import shamu.company.user.pojo.UserRoleUpdatePojo;
import shamu.company.user.pojo.UserStatusUpdatePojo;
import shamu.company.user.service.UserService;
import shamu.company.utils.Auth0Util;
import shamu.company.utils.AwsUtil;
import shamu.company.utils.AwsUtil.Type;
import shamu.company.utils.FileValidateUtil;
import shamu.company.utils.FileValidateUtil.FileType;

@RestApiController
public class UserRestController extends BaseRestController {

  private final UserService userService;

  private final CompanyService companyService;

  private final AwsUtil awsUtil;

  private final UserMapper userMapper;

  private final Auth0Util auth0Util;

  public UserRestController(final UserService userService, final CompanyService companyService,
      final AwsUtil awsUtil, final UserMapper userMapper,
      final Auth0Util auth0Util) {
    this.userService = userService;
    this.companyService = companyService;
    this.awsUtil = awsUtil;
    this.userMapper = userMapper;
    this.auth0Util = auth0Util;
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
    final Role userRole = auth0Util.getUserRole(getAuthUser().getEmail());
    if (getAuthUser().getId().equals(id)
        || userRole == Role.ADMIN) {
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
    //TODO: Need an appropriate file size.
    FileValidateUtil
        .validate(file, 2 * FileValidateUtil.MB, FileType.JPEG, FileType.PNG, FileType.GIF);
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
  public boolean resetPassword(@RequestBody @Valid final UpdatePasswordDto updatePasswordDto) {
    userService.resetPassword(updatePasswordDto);
    return true;
  }

  @PatchMapping("user/password/update")
  public void updatePassword(@RequestBody @Valid final ChangePasswordPojo changePasswordPojo) {
    userService.updatePassword(changePasswordPojo, getAuthUser().getEmail());
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
    User user = userService.findUserById(id);
    user = userService.updateUserRole(getAuthUser().getEmail(), userRoleUpdatePojo, user);
    final UserRoleAndStatusInfoDto resultInformation = userMapper
        .convertToUserRoleAndStatusInfoDto(user);
    final Role userRole = auth0Util.getUserRole(user.getUserContactInformation().getEmailWork());
    resultInformation.setUserRole(userRole.getValue());
    return resultInformation;
  }

  @PatchMapping("users/{id}/inactivate")
  @PreAuthorize("hasPermission(#id, 'USER', 'VIEW_SETTING')")
  public UserRoleAndStatusInfoDto inactivateUser(@PathVariable @HashidsFormat final Long id,
      @RequestBody final UserStatusUpdatePojo userStatusUpdatePojo) {
    User user = userService.findUserById(id);
    user = userService.inactivateUser(getAuthUser().getEmail(), userStatusUpdatePojo, user);
    final Role userRole = auth0Util.getUserRole(user.getUserContactInformation().getEmailWork());
    final UserRoleAndStatusInfoDto resultInformation =
        userMapper.convertToUserRoleAndStatusInfoDto(user);
    resultInformation.setUserRole(userRole.getValue());
    return resultInformation;
  }

  @GetMapping("users/all")
  public List<UserDto> getAllUsers() {
    final List<User> users = userService.findAllUsersByCompany(getCompanyId());
    return userMapper.convertToUserDtos(users);
  }

  @GetMapping("current/user-info")
  public CurrentUserDto getUserInfo() {
    return userService.getCurrentUserInfo(getUserId());
  }
}
