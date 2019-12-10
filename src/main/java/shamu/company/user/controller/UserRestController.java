package shamu.company.user.controller;

import java.io.IOException;
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
import shamu.company.benefit.entity.RetirementType;
import shamu.company.common.BaseRestController;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.validation.constraints.FileValidate;
import shamu.company.employee.dto.EmailResendDto;
import shamu.company.job.entity.CompensationFrequency;
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
import shamu.company.user.entity.CompensationOvertimeStatus;
import shamu.company.user.entity.DeactivationReasons;
import shamu.company.user.entity.Ethnicity;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.repository.CompensationFrequencyRepository;
import shamu.company.user.repository.CompensationOvertimeStatusRepository;
import shamu.company.user.repository.DeactivationReasonRepository;
import shamu.company.user.repository.EthnicityRepository;
import shamu.company.user.repository.GenderRepository;
import shamu.company.user.repository.MaritalStatusRepository;
import shamu.company.user.repository.RetirementTypeRepository;
import shamu.company.user.repository.UserRolesRepository;
import shamu.company.user.repository.UserStatusRepository;
import shamu.company.user.service.UserService;
import shamu.company.utils.ReflectionUtil;

@RestApiController
@Validated
public class UserRestController extends BaseRestController {

  private final UserService userService;

  private final UserMapper userMapper;

  private final CompensationFrequencyRepository frequencyRepository;

  private final CompensationOvertimeStatusRepository compensationOvertimeStatusRepository;

  private final DeactivationReasonRepository deactivationReasonRepository;

  private final EthnicityRepository ethnicityRepository;

  private final GenderRepository genderRepository;

  private final MaritalStatusRepository maritalStatusRepository;

  private final RetirementTypeRepository retirementTypeRepository;

  private final UserRolesRepository userRolesRepository;

  private final UserStatusRepository userStatusRepository;

  public UserRestController(final UserService userService,
      final UserMapper userMapper,
      final CompensationFrequencyRepository frequencyRepository,
      final CompensationOvertimeStatusRepository compensationOvertimeStatusRepository,
      final DeactivationReasonRepository deactivationReasonRepository,
      final EthnicityRepository ethnicityRepository,
      final GenderRepository genderRepository,
      final MaritalStatusRepository maritalStatusRepository,
      final RetirementTypeRepository retirementTypeRepository,
      final UserRolesRepository userRolesRepository,
      final UserStatusRepository userStatusRepository) {
    this.userService = userService;
    this.userMapper = userMapper;
    this.frequencyRepository = frequencyRepository;
    this.compensationOvertimeStatusRepository = compensationOvertimeStatusRepository;
    this.deactivationReasonRepository = deactivationReasonRepository;
    this.ethnicityRepository = ethnicityRepository;
    this.genderRepository = genderRepository;
    this.maritalStatusRepository = maritalStatusRepository;
    this.retirementTypeRepository = retirementTypeRepository;
    this.userRolesRepository = userRolesRepository;
    this.userStatusRepository = userStatusRepository;
  }

  @PostMapping(value = "user/sign-up")
  public HttpEntity signUp(@RequestBody final UserSignUpDto signUpDto) {
    userService.signUp(signUpDto);
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping(value = "user/check/email/{email}")
  public Boolean checkEmail(@PathVariable final String email) {
    return userService.existsByEmailWork(email);
  }

  @GetMapping(value = "users/{id}/head-portrait")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_USER_PERSONAL')")
  public String getHeadPortrait(@PathVariable final String id) {
    final User currentUser = userService.findByUserId(getUserId());
    final Role userRole = currentUser.getRole();
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
      @PathVariable final String id,
      @RequestParam("file")
      //TODO: Need an appropriate file size.
      @FileValidate(maxSize = 2 * 1024 * 1024, fileType = {"JPEG", "PNG", "GIF"})
      final MultipartFile file
  ) throws IOException {
    return userService.handleUploadFile(id, file);
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
  public void updatePassword(@RequestBody @Valid final ChangePasswordDto changePasswordDto) {
    userService.updatePassword(changePasswordDto, getUserId());
  }

  @PreAuthorize("hasPermission(#id,'USER', 'EDIT_SELF')")
  @GetMapping("users/{id}/account-info")
  public AccountInfoDto getPreSetAccountInfo(@PathVariable final String id) {
    return userService.getPreSetAccountInfoByUserId(id);
  }

  @GetMapping("users/{id}/avatar")
  public UserAvatarDto getUserAvatar(@PathVariable final String id) {
    final User user = userService.findById(id);
    return userMapper.convertToUserAvatarDto(user);
  }

  @PatchMapping("users/{id}/user-role")
  @PreAuthorize("hasPermission(#id, 'USER', 'VIEW_SETTING')")
  public UserRoleAndStatusInfoDto updateUserRole(@PathVariable final String id,
      @RequestBody final UserRoleUpdateDto userRoleUpdateDto) {
    User user = userService.findById(id);
    user = userService.updateUserRole(getAuthUser().getEmail(), userRoleUpdateDto, user);
    return userMapper
        .convertToUserRoleAndStatusInfoDto(user);
  }

  @PatchMapping("users/{id}/deactivate")
  @PreAuthorize("hasPermission(#id, 'USER', 'DEACTIVATE_USER')")
  public UserRoleAndStatusInfoDto deactivateUser(@PathVariable final String id,
      @RequestBody final UserStatusUpdateDto userStatusUpdateDto) {
    User user = userService.findById(id);
    user = userService.deactivateUser(getAuthUser().getEmail(), userStatusUpdateDto, user);
    return userMapper.convertToUserRoleAndStatusInfoDto(user);
  }

  @DeleteMapping("users/{id}/delete")
  @PreAuthorize("hasPermission(#id, 'USER', 'EDIT_USER')")
  public void deleteUser(@PathVariable final String id) {
    final User employee = userService.findById(id);
    userService.deleteUser(employee);
  }

  @GetMapping("users/all")
  public List<UserDto> getAllUsers() {
    final List<User> users = userService.findAllUsersByCompany(getCompanyId());
    return userMapper.convertToUserDtos(users);
  }

  @GetMapping("current/user-info")
  public CurrentUserDto getUserInfo(final HttpServletRequest request) {
    final String mockId = request.getHeader("X-Mock-To");
    if (Strings.isBlank(mockId)) {
      final CurrentUserDto userDto = userService
          .getCurrentUserInfo(getAuthentication().getUserId());
      userService.cacheUser(getToken(), userDto.getId());
      return userDto;
    }

    final User user = userService.findByUserId(getAuthentication().getUserId());
    final Role role = user.getRole();
    if (role != Role.SUPER_ADMIN) {
      throw new ForbiddenException("You are not super admin!");
    }

    final String useId = mockId;
    userService.cacheUser(getToken(), useId);
    return userService.getMockUserInfo(useId);
  }

  @GetMapping("/user/check-password/{password}")
  @PreAuthorize("hasAuthority('EDIT_SELF')")
  public void checkPassword(@PathVariable final String password) {
    userService.checkPassword(getAuthUser().getEmail(), password);
  }

  @PatchMapping("/user/send-verify-email")
  @PreAuthorize(
      "hasPermission(#emailResendDto.userId,'USER', 'EDIT_USER')"
          + " or hasPermission(#emailResendDto.userId,'USER', 'EDIT_SELF')")
  public void sendChangeWorkEmail(@RequestBody @Valid final EmailResendDto emailResendDto) {
    userService.sendChangeWorkEmail(emailResendDto.getUserId(),emailResendDto.getEmail());

  }

  @GetMapping("/user/{id}/change-work-email")
  @PreAuthorize("hasPermission(#id, 'USER', 'VIEW_CHANGING_WORK_EMAIL')")
  public String getChangeWorkEmail(@PathVariable @Valid final String id) {
    final User user = userService.findById(id);
    return user.getChangeWorkEmail();
  }

  @GetMapping("/user/change-work-email")
  @PreAuthorize("hasAuthority('EDIT_SELF')")
  public String getChangeWorkEmail() {
    final User user = userService.findById(getAuthUser().getId());
    return user.getChangeWorkEmail();
  }

  @GetMapping("/user/send-verify-work-email")
  @PreAuthorize("hasAuthority('EDIT_SELF')")
  public void sendVerifyChangeWorkEmail() {
    final User user = userService.findById(getAuthUser().getId());
    userService.sendVerifyChangeWorkEmail(user);
  }

  @GetMapping("compensation-frequencies")
  public List<CommonDictionaryDto> getCompensationFrequencies() {
    final List<CompensationFrequency> frequencies = frequencyRepository.findAll();
    return ReflectionUtil.convertTo(frequencies, CommonDictionaryDto.class);
  }

  @GetMapping("compensation-overtime-statuses")
  public List<CommonDictionaryDto> getCompensationStatuses() {
    final List<CompensationOvertimeStatus> overtimeStatuses =
        compensationOvertimeStatusRepository.findAll();
    return ReflectionUtil.convertTo(overtimeStatuses, CommonDictionaryDto.class);
  }
  
  @GetMapping("deactivation-reasons")
  public List<CommonDictionaryDto> getDeactivationReasons() {
    final List<DeactivationReasons> deactivationReasons = deactivationReasonRepository
        .findAll();
    return ReflectionUtil.convertTo(deactivationReasons, CommonDictionaryDto.class);
  }

  @GetMapping("ethnicities")
  public List<CommonDictionaryDto> getEthnicities() {
    final List<Ethnicity> ethnicities = ethnicityRepository.findAll();
    return ReflectionUtil.convertTo(ethnicities, CommonDictionaryDto.class);
  }

  @GetMapping("genders")
  public List<CommonDictionaryDto> getGenders() {
    final List<Gender> genders = genderRepository.findAll();
    return ReflectionUtil.convertTo(genders, CommonDictionaryDto.class);
  }

  @GetMapping("marital-statuses")
  public List<CommonDictionaryDto> getMaritalStatuses() {
    final List<MaritalStatus> maritalStatuses = maritalStatusRepository.findAll();
    return ReflectionUtil.convertTo(maritalStatuses, CommonDictionaryDto.class);
  }

  @GetMapping("retirement-types")
  public List<CommonDictionaryDto> getRetirementTypes() {
    final List<RetirementType> retirementTypes = retirementTypeRepository.findAll();
    return ReflectionUtil.convertTo(retirementTypes, CommonDictionaryDto.class);
  }


  @GetMapping("user-roles")
  public List<CommonDictionaryDto> getAllUserRoles() {
    final List<UserRole> userRoles = userRolesRepository.findAll();
    return ReflectionUtil.convertTo(userRoles, CommonDictionaryDto.class);
  }

  @GetMapping("user-statuses")
  public List<CommonDictionaryDto> getAllUserStatuses() {
    final List<UserStatus> userStatuses = userStatusRepository.findAll();
    return ReflectionUtil.convertTo(userStatuses, CommonDictionaryDto.class);
  }
}
