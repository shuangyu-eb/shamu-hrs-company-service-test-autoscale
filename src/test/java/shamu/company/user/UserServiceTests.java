package shamu.company.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auth0.json.auth.CreatedUser;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.ITemplateEngine;
import shamu.company.admin.entity.SystemAnnouncement;
import shamu.company.admin.service.SystemAnnouncementsService;
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.service.OvertimeService;
import shamu.company.authorization.PermissionUtils;
import shamu.company.client.DocumentClient;
import shamu.company.common.entity.Country;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.exception.errormapping.AlreadyExistsException;
import shamu.company.common.exception.errormapping.EmailAlreadyVerifiedException;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;
import shamu.company.common.service.DepartmentService;
import shamu.company.company.entity.Company;
import shamu.company.company.repository.CompanyRepository;
import shamu.company.company.service.CompanyBenefitsSettingService;
import shamu.company.company.service.CompanyService;
import shamu.company.crypto.SecretHashRepository;
import shamu.company.email.service.EmailService;
import shamu.company.employee.dto.EmailUpdateDto;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.employee.dto.OrgChartDto;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.helpers.s3.Type;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.job.service.JobService;
import shamu.company.job.service.JobUserService;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.scheduler.DynamicScheduler;
import shamu.company.scheduler.QuartzJobScheduler;
import shamu.company.server.dto.AuthUser;
import shamu.company.timeoff.service.PaidHolidayService;
import shamu.company.user.dto.*;
import shamu.company.user.entity.DismissedAt;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.entity.mapper.UserAddressMapper;
import shamu.company.user.entity.mapper.UserContactInformationMapper;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.exception.errormapping.AuthenticationFailedException;
import shamu.company.user.exception.errormapping.EmailExpiredException;
import shamu.company.user.exception.errormapping.PasswordDuplicatedException;
import shamu.company.user.exception.errormapping.UserNotFoundByEmailException;
import shamu.company.user.exception.errormapping.UserNotFoundByInvitationTokenException;
import shamu.company.user.exception.errormapping.WorkEmailDuplicatedException;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.service.DismissedAtService;
import shamu.company.user.service.UserAccessLevelEventService;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserBenefitsSettingService;
import shamu.company.user.service.UserContactInformationService;
import shamu.company.user.service.UserPersonalInformationService;
import shamu.company.user.service.UserRoleService;
import shamu.company.user.service.UserService;
import shamu.company.user.service.UserStatusService;
import shamu.company.utils.UuidUtil;

class UserServiceTests {

  @InjectMocks private UserService userService;

  @Mock private ITemplateEngine templateEngine;
  @Mock private UserRepository userRepository;
  @Mock private SecretHashRepository secretHashRepository;
  @Mock private JobUserService jobUserService;
  @Mock private UserStatusService userStatusService;
  @Mock private EmailService emailService;
  @Mock private UserPersonalInformationMapper userPersonalInformationMapper;
  @Mock private UserEmergencyContactService userEmergencyContactService;
  @Mock private UserAddressService userAddressService;
  @Mock private PaidHolidayService paidHolidayService;
  @Mock private CompanyService companyService;
  @Mock private UserContactInformationMapper userContactInformationMapper;
  @Mock private UserAddressMapper userAddressMapper;
  @Mock private Auth0Helper auth0Helper;
  @Mock private UserAccessLevelEventService userAccessLevelEventService;
  @Mock private DepartmentService departmentService;
  @Mock private JobService jobService;
  @Mock private UserMapper userMapper;
  @Mock private UserContactInformationService userContactInformationService;
  @Mock private UserPersonalInformationService userPersonalInformationService;
  @Mock private AuthUserCacheManager authUserCacheManager;
  @Mock private DynamicScheduler dynamicScheduler;
  @Mock private QuartzJobScheduler quartzJobScheduler;
  @Mock private AwsHelper awsHelper;
  @Mock private UserRoleService userRoleService;
  @Mock private PermissionUtils permissionUtils;
  @Mock private CompanyBenefitsSettingService companyBenefitsSettingService;
  @Mock private UserBenefitsSettingService userBenefitsSettingService;
  @Mock private EntityManager entityManager;
  @Mock private CompanyRepository companyRepository;
  @Mock private SystemAnnouncementsService systemAnnouncementsService;
  @Mock private DismissedAtService dismissedAtService;
  @Mock private DocumentClient documentClient;
  @Mock private OvertimeService overtimeService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void testCacheUser() {
    final String token = "token";
    final String userId = UUID.randomUUID().toString().replaceAll("-", "");
    final User cachedUser = new User();
    Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(cachedUser));
    Mockito.when(userMapper.convertToAuthUser(cachedUser)).thenReturn(new AuthUser());
    Assertions.assertDoesNotThrow(() -> userService.cacheUser(token, userId));
  }

  @Test
  void testFindAllEmployees() {
    final String userId = UUID.randomUUID().toString().replaceAll("-", "");
    final EmployeeListSearchCondition employeeListSearchCondition =
        new EmployeeListSearchCondition();
    final User currentUser = new User();
    Mockito.when(permissionUtils.hasAuthority(Mockito.anyString())).thenReturn(false);
    Mockito.when(userRepository.findActiveUserById(userId)).thenReturn(currentUser);
    Assertions.assertDoesNotThrow(() -> userService.findAllEmployees(employeeListSearchCondition));
  }

  @Test
  void testGetPreSetAccountInfoByUserId() {
    final String userId = UUID.randomUUID().toString().replaceAll("-", "");
    final User user = new User();
    Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    Assertions.assertDoesNotThrow(() -> userService.getPreSetAccountInfoByUserId(userId));
  }

  @Test
  void testDeleteHeadPortrait() {
    final String userId = "userId";
    final User user = new User();
    user.setImageUrl("url");
    Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    Assertions.assertDoesNotThrow(() -> userService.handleDeleteHeadPortrait(userId));
  }

  @Nested
  class testGetCurrentUserInfo {
    User currentUser;
    String userId = UUID.randomUUID().toString().replaceAll("-", "");
    UserPersonalInformation userPersonalInformation;
    Company company;
    UserRole userRole;

    @BeforeEach
    void init() {
      currentUser = new User();
      currentUser.setId(userId);
      userPersonalInformation = new UserPersonalInformation();
      userPersonalInformation.setFirstName("Aa");
      userPersonalInformation.setLastName("Bb");
      currentUser.setUserPersonalInformation(userPersonalInformation);
      currentUser.setImageUrl(RandomStringUtils.randomAlphabetic(11));
      company = new Company();
      company.setId("1");
      userRole = new UserRole();
      userRole.setName("admin");
      Mockito.when(userRepository.findByManagerUser(Mockito.any()))
          .thenReturn(Collections.emptyList());
    }

    @Test
    void whenVerifiedAtIsNull_thenShouldSuccess() {
      Mockito.when(userRepository.findById(Mockito.anyString()))
          .thenReturn(java.util.Optional.of(currentUser));
      final CurrentUserDto userInfo = userService.getCurrentUserInfo(currentUser.getId());
      Assertions.assertEquals(userInfo.getId(), currentUser.getId());
      Assertions.assertFalse(userInfo.getVerified());
    }

    @Test
    void whenVerifiedAtIsNotNull_thenShouldSuccess() {
      currentUser.setVerifiedAt(new Timestamp(System.currentTimeMillis()));
      currentUser.setUserRole(userRole);
      Mockito.when(userRepository.findById(Mockito.anyString()))
          .thenReturn(java.util.Optional.of(currentUser));
      final CurrentUserDto userInfo = userService.getCurrentUserInfo(currentUser.getId());
      Assertions.assertEquals(userInfo.getId(), currentUser.getId());
      Assertions.assertTrue(userInfo.getVerified());
    }
  }

  @Nested
  class TestResetPassword {

    private UpdatePasswordDto updatePasswordDto;

    @BeforeEach
    void init() {
      updatePasswordDto = new UpdatePasswordDto();
      updatePasswordDto.setNewPassword(RandomStringUtils.randomAlphabetic(10));
      updatePasswordDto.setResetPasswordToken(RandomStringUtils.randomAlphabetic(10));
    }

    @Test
    void whenUserNotExist_thenShouldThrow() {
      Mockito.when(
              userRepository.findByResetPasswordToken(updatePasswordDto.getResetPasswordToken()))
          .thenReturn(null);
      assertThatExceptionOfType(UserNotFoundByInvitationTokenException.class)
          .isThrownBy(() -> userService.resetPassword(updatePasswordDto));
    }

    @Test
    void whenAuthUserNotExist_thenShouldThrow() {
      Mockito.when(
              userRepository.findByResetPasswordToken(updatePasswordDto.getResetPasswordToken()))
          .thenReturn(new User());
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.anyString())).thenReturn(null);
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> userService.resetPassword(updatePasswordDto));
    }

    @Test
    void whenPasswordIsInvalid_thenShouldThrow() {
      final User user = new User();
      user.setId(UuidUtil.getUuidString());
      final UserContactInformation userContactInformation = new UserContactInformation();
      userContactInformation.setEmailWork("example@indeed.com");
      user.setUserContactInformation(userContactInformation);
      Mockito.when(
              userRepository.findByResetPasswordToken(updatePasswordDto.getResetPasswordToken()))
          .thenReturn(user);
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(user.getId()))
          .thenReturn(new com.auth0.json.mgmt.users.User());
      Mockito.when(
              auth0Helper.isPasswordValid(
                  user.getUserContactInformation().getEmailWork(),
                  updatePasswordDto.getNewPassword()))
          .thenReturn(Boolean.TRUE);
      assertThatExceptionOfType(PasswordDuplicatedException.class)
          .isThrownBy(() -> userService.resetPassword(updatePasswordDto));
    }

    @Test
    void whenUserExist_passwordValid_thenShouldSuccess() {
      final User databaseUser = new User();
      final UserContactInformation userContactInformation = new UserContactInformation();
      userContactInformation.setEmailWork("example@indeed.com");
      databaseUser.setUserContactInformation(userContactInformation);
      final UserStatus targetStatus = new UserStatus();
      targetStatus.setName(Status.PENDING_VERIFICATION.name());
      databaseUser.setUserStatus(targetStatus);

      final com.auth0.json.mgmt.users.User authUser = new com.auth0.json.mgmt.users.User();

      Mockito.when(
              userRepository.findByResetPasswordToken(updatePasswordDto.getResetPasswordToken()))
          .thenReturn(databaseUser);
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any())).thenReturn(authUser);
      Mockito.when(userStatusService.findByName(Mockito.any())).thenReturn(targetStatus);

      Assertions.assertDoesNotThrow(() -> userService.resetPassword(updatePasswordDto));
    }
  }

  @Test
  void testDismissCurrentActiveAnnouncement() {
    final DismissedAt dismissed = new DismissedAt();
    final User user = new User(UuidUtil.getUuidString());
    final SystemAnnouncement systemAnnouncement = new SystemAnnouncement();

    Mockito.when(
            dismissedAtService.findByUserIdAndSystemAnnouncementId(Mockito.any(), Mockito.any()))
        .thenReturn(null);
    Mockito.when(systemAnnouncementsService.findById(Mockito.any())).thenReturn(systemAnnouncement);
    Mockito.when(dismissedAtService.save(Mockito.any())).thenReturn(dismissed);
    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));

    Assertions.assertDoesNotThrow(
        () -> userService.dismissCurrentActiveAnnouncement(UuidUtil.getUuidString(), "1"));
  }

  @Nested
  class testGetOrgChart {

    private String userId;
    private String companyId;

    @BeforeEach
    void init() {
      userId = UuidUtil.getUuidString();
      companyId = UuidUtil.getUuidString();
    }

    @Test
    void whenManagerIsNotNull_thenShouldNotThrow() {
      final OrgChartDto manager = new OrgChartDto();
      final List<OrgChartDto> orgChartUserItemList = new ArrayList<>();
      manager.setId(UUID.randomUUID().toString().replaceAll("-", ""));
      orgChartUserItemList.add(manager);
      Mockito.when(userRepository.findOrgChartItemByUserId(userId)).thenReturn(manager);
      Mockito.when(userRepository.findOrgChartItemByManagerId(manager.getId()))
          .thenReturn(orgChartUserItemList);
      Assertions.assertDoesNotThrow(() -> userService.getOrgChart(userId));
    }

    @Test
    void whenUserIdIsNull_thenShouldCall() {
      userId = null;
      final OrgChartDto orgChartDto = new OrgChartDto();
      orgChartDto.setId(companyId);

      Mockito.when(userRepository.findOrgChartItemByManagerId(null)).thenReturn(new ArrayList<>());
      Mockito.when(userMapper.convertOrgChartDto(Mockito.any())).thenReturn(orgChartDto);
      Mockito.when(userRepository.countExistingUser()).thenReturn(100);
      userService.getOrgChart(userId);
      Mockito.verify(userRepository, Mockito.times(1)).findOrgChartItemByManagerId(null);
    }
  }

  @Nested
  class UpdateUserRole {

    private String email;
    private UserRoleUpdateDto userRoleUpdateDto;
    private String userId;
    private User user;

    @BeforeEach
    void init() {
      user = new User();
      userId = UUID.randomUUID().toString().replaceAll("-", "");
      user.setId(userId);
      userRoleUpdateDto = new UserRoleUpdateDto();
    }

    @Test
    void whenCurrentUserAdminAndHasUnderling_thenShouldBeManager() {
      userRoleUpdateDto.setUserRole(Role.ADMIN);
      final List<User> underlings = new ArrayList<>();
      underlings.add(new User());
      Mockito.when(userRepository.findAllByManagerUserId(userId)).thenReturn(underlings);
      userService.updateUserRole(email, userRoleUpdateDto, user);
      Mockito.verify(userRoleService, Mockito.times(1)).getManager();
    }

    @Test
    void whenCurrentUserAdminAndHasNoUnderling_thenShouldBeEmployee() {
      userRoleUpdateDto.setUserRole(Role.ADMIN);
      final List<User> underlings = new ArrayList<>();
      Mockito.when(userRepository.findAllByManagerUserId(userId)).thenReturn(underlings);
      userService.updateUserRole(email, userRoleUpdateDto, user);
      Mockito.verify(userRoleService, Mockito.times(1)).getEmployee();
    }

    @Test
    void whenCurrentUserNotAdmin_thenShouldBeAdmin() {
      userRoleUpdateDto.setUserRole(Role.MANAGER);
      userService.updateUserRole(email, userRoleUpdateDto, user);
      Mockito.verify(userRoleService, Mockito.times(1)).getAdmin();
    }
  }

  @Nested
  class DeactivateUser {

    private String email;
    private UserStatusUpdateDto userStatusUpdateDto;
    private User user;
    private String userId;

    @BeforeEach
    void init() {
      userStatusUpdateDto = new UserStatusUpdateDto();
      userStatusUpdateDto.setUserStatus(Status.ACTIVE);
      userStatusUpdateDto.setDeactivationReason(new SelectFieldInformationDto("id", "name"));
      user = new User();
      final UserRole userRole = new UserRole();
      userId = UUID.randomUUID().toString().replaceAll("-", "");
      userRole.setName(Role.ADMIN.name());
      user.setUserRole(userRole);
      user.setId(userId);
    }

    @Test
    void whenDeactiveIsNow_thenShouldNotScheduler() {
      userStatusUpdateDto.setDeactivationDate(new Date(System.currentTimeMillis()));
      Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      userService.deactivateUser(email, userStatusUpdateDto, user);

      Mockito.verify(userRepository, Mockito.times(2)).save(user);
    }

    @Test
    void whenDeactiveIsNotNow_thenShouldScheduler() {
      userStatusUpdateDto.setDeactivationDate(new Date(System.nanoTime()));
      Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      userService.deactivateUser(email, userStatusUpdateDto, user);

      Mockito.verify(userRepository, Mockito.times(1)).save(user);
      Mockito.verify(quartzJobScheduler, Mockito.times(1))
          .addOrUpdateJobSchedule(
              Mockito.any(),
              Mockito.anyString(),
              Mockito.anyString(),
              Mockito.anyMap(),
              Mockito.any());
    }
  }

  @Nested
  class UpdatePassword {

    private ChangePasswordDto changePasswordDto;
    private String userId;
    private com.auth0.json.mgmt.users.User user;

    @BeforeEach
    void init() {
      user = new com.auth0.json.mgmt.users.User();
      user.setEmail("testEmail");
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(userId)).thenReturn(user);
      changePasswordDto = new ChangePasswordDto();
      changePasswordDto.setPassword("password");
    }

    @Test
    void whenPasswordIsSame_thenShouldThrow() {
      changePasswordDto.setNewPassword("password");
      assertThatExceptionOfType(AuthenticationFailedException.class)
          .isThrownBy(() -> userService.updatePassword(changePasswordDto, userId));
    }

    @Test
    void whenPasswordIsError_thenShouldThrow() {
      Mockito.when(auth0Helper.isPasswordValid(user.getEmail(), changePasswordDto.getPassword()))
          .thenReturn(false);
      assertThatExceptionOfType(AuthenticationFailedException.class)
          .isThrownBy(() -> userService.updatePassword(changePasswordDto, userId));
    }

    @Test
    void whenOk_thenShouldSendEmail() {
      changePasswordDto.setNewPassword("newPassword");
      Mockito.when(auth0Helper.isPasswordValid(user.getEmail(), changePasswordDto.getPassword()))
          .thenReturn(true);
      userService.updatePassword(changePasswordDto, userId);

      Mockito.verify(emailService, Mockito.times(1)).saveAndScheduleEmail(Mockito.any());
    }
  }

  @Nested
  class checkSSN {

    private UserPersonalInformation userPersonalInformation;

    @Test
    void whenPersonalInfoIsNotComplete_thenReturnFalse() {
      final User user = new User();
      final String userId = UuidUtil.getUuidString();
      user.setId(userId);
      userPersonalInformation = new UserPersonalInformation();
      user.setUserPersonalInformation(userPersonalInformation);
      Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      Assertions.assertFalse(userService.checkPersonalInfoComplete(userId));
    }

    @Test
    void whenPersonalInfoIsComplete_thenReturnTrue() {
      final User user = new User();
      final String userId = UuidUtil.getUuidString();
      user.setId(userId);
      userPersonalInformation = new UserPersonalInformation();
      userPersonalInformation.setSsn("1111");
      userPersonalInformation.setBirthDate(new Date(123));
      user.setUserPersonalInformation(userPersonalInformation);
      final UserAddress userAddress = new UserAddress();
      userAddress.setCity("112");
      userAddress.setCountry(new Country());
      userAddress.setStateProvince(new StateProvince());
      userAddress.setStreet1("123");
      Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      Mockito.when(userAddressService.findUserAddressByUserId(user.getId()))
          .thenReturn(userAddress);
      Assertions.assertTrue(userService.checkPersonalInfoComplete(userId));
    }
  }

  @Nested
  class UpdateWorkEmail {

    private String userId;
    private EmailUpdateDto emailUpdateDto;

    private final String currentUserPassword = "password";

    @BeforeEach
    void init() {
      final User user = new User();
      final UserContactInformation contactInformation = new UserContactInformation();
      contactInformation.setEmailWork("example@example.com");
      user.setUserContactInformation(contactInformation);

      userId = UuidUtil.getUuidString();
      user.setId(userId);
      Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      emailUpdateDto = new EmailUpdateDto();
      emailUpdateDto.setEmail("example1@example.com");
      emailUpdateDto.setPassword(RandomStringUtils.randomAlphanumeric(16));
      emailUpdateDto.setUserId(userId);

      Mockito.when(auth0Helper.isPasswordValid(Mockito.anyString(), Mockito.anyString()))
          .thenReturn(true);
    }

    @Test
    void whenPasswordIsNotValid_thenShouldThrow() {
      Mockito.when(auth0Helper.isPasswordValid(Mockito.anyString(), Mockito.anyString()))
          .thenReturn(false);
      assertThatExceptionOfType(AuthenticationFailedException.class)
          .isThrownBy(() -> userService.updateWorkEmail(emailUpdateDto, currentUserPassword));
    }

    @Test
    void whenEmailIsSame_thenShouldThrow() {
      emailUpdateDto.setEmail("example@example.com");
      assertThatExceptionOfType(WorkEmailDuplicatedException.class)
          .isThrownBy(() -> userService.updateWorkEmail(emailUpdateDto, currentUserPassword));
    }

    @Test
    void whenNewEmailIsUsed_thenShouldThrow() {
      Mockito.when(auth0Helper.existsByEmail(emailUpdateDto.getEmail())).thenReturn(true);
      assertThatExceptionOfType(AlreadyExistsException.class)
          .isThrownBy(() -> userService.updateWorkEmail(emailUpdateDto, currentUserPassword));
    }

    @Test
    void whenOk_thenShouldSendEmail() {
      Mockito.when(auth0Helper.existsByEmail(emailUpdateDto.getEmail())).thenReturn(false);
      userService.updateWorkEmail(emailUpdateDto, currentUserPassword);

      Mockito.verify(emailService, Mockito.times(1)).handleEmail(Mockito.any());
      Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
    }
  }

  @Nested
  class ChangeWorkEmailTokenExist {

    private String token;

    @Test
    void whenWorkEmailIsNotExist_thenShouldReturnFalse() {
      Mockito.when(userRepository.existsByChangeWorkEmailToken(token)).thenReturn(false);
      Assertions.assertFalse(userService.changeWorkEmailTokenExist(token));
    }

    @Test
    void whenWorkEmailIsExist_thenShouldReturnTrue() {
      final User currentUser = new User();
      final UserContactInformation contactInformation = new UserContactInformation();
      currentUser.setUserContactInformation(contactInformation);
      currentUser.setChangeWorkEmail("email");
      currentUser.setId(UUID.randomUUID().toString().replaceAll("-", ""));
      Mockito.when(userRepository.existsByChangeWorkEmailToken(token)).thenReturn(true);
      Mockito.when(userRepository.findByChangeWorkEmailToken(token)).thenReturn(currentUser);

      Assertions.assertTrue(userService.changeWorkEmailTokenExist(token));
    }
  }

  @Nested
  class UploadFile {

    MultipartFile file;
    private String id;

    @Test
    void whenPathIsNull_thenShouldReturnNull() {
      Mockito.when(awsHelper.uploadFile(file, Type.IMAGE)).thenReturn(null);
      Assertions.assertNull(userService.handleUploadFile(id, file));
    }

    @Test
    void whenOriginalPathIsNotNull_thenShouldDelete() {
      Mockito.when(awsHelper.uploadFile(file, Type.IMAGE)).thenReturn("path");
      final User user = new User();
      user.setImageUrl("url");
      Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(user));
      userService.handleUploadFile(id, file);
      Mockito.verify(awsHelper, Mockito.times(1)).deleteFile(Mockito.anyString());
    }
  }

  @Nested
  class ResendVerifyEmail {

    private final com.auth0.json.mgmt.users.User auth0User = new com.auth0.json.mgmt.users.User();
    private String email;

    @Test
    void whenUserIsVerified_thenShouldThrow() {
      auth0User.setEmailVerified(true);
      Mockito.when(auth0Helper.findByEmail(email)).thenReturn(auth0User);
      assertThatExceptionOfType(EmailAlreadyVerifiedException.class)
          .isThrownBy(() -> userService.resendVerificationEmail(email));
    }

    @Test
    void whenOk_thenShouldSendEmail() {
      auth0User.setEmailVerified(false);
      auth0User.setId(UUID.randomUUID().toString().replaceAll("-", ""));
      Mockito.when(auth0Helper.findByEmail(email)).thenReturn(auth0User);
      userService.resendVerificationEmail(email);

      Mockito.verify(auth0Helper, Mockito.times(1)).sendVerificationEmail(Mockito.anyString());
    }
  }

  @Nested
  class SignUp {

    private String userId;
    private UserSignUpDto userSignUpDto;

    @BeforeEach
    void init() {
      userId = UUID.randomUUID().toString().replaceAll("-", "");
      userSignUpDto =
          UserSignUpDto.builder()
              .companyName(RandomStringUtils.randomAlphabetic(4))
              .firstName(RandomStringUtils.randomAlphabetic(3))
              .lastName(RandomStringUtils.randomAlphabetic(3))
              .workEmail("test@qq.com")
              .build();
    }

    @Test
    void whenCanFindEmail_thenShouldSuccess() {
      final com.auth0.json.mgmt.users.User auth0User = new com.auth0.json.mgmt.users.User();
      auth0User.setEmail("example@mail.com");
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(userId)).thenReturn(auth0User);
      final Company company = new Company();
      company.setId(RandomStringUtils.randomAlphabetic(16));
      company.setName("companyInfo");
      Mockito.when(companyService.save(Mockito.any())).thenReturn(company);
      Mockito.when(companyService.getCompany()).thenReturn(company);
      Mockito.when(userStatusService.findByName(Mockito.any()))
          .thenReturn(new UserStatus(Status.ACTIVE.name()));

      final User persistedUser = new User();
      persistedUser.setUserContactInformation(new UserContactInformation());
      persistedUser.getUserContactInformation().setEmailWork("example@example.com");
      persistedUser.setUserPersonalInformation(new UserPersonalInformation());
      persistedUser.getUserPersonalInformation().setFirstName("F");
      persistedUser.getUserPersonalInformation().setLastName("L");
      Mockito.when(userRepository.save(Mockito.any())).thenReturn(persistedUser);
      Mockito.doNothing().when(secretHashRepository).generateCompanySecretByCompanyId("companyId");
      final com.auth0.json.mgmt.users.User user = new com.auth0.json.mgmt.users.User();
      final CreatedUser createdUser = new CreatedUser();
      Mockito.when(auth0Helper.signUp(Mockito.any(), Mockito.any())).thenReturn(createdUser);
      Mockito.when(auth0Helper.updateAuthUserAppMetaData(Mockito.anyString()))
          .thenReturn(user);
      userService.signUp(userSignUpDto, "123");
      Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
    }
  }

  @Nested
  class HasUserAccess {

    User currentUser;
    String targetUserId;

    @BeforeEach
    void setUp() {
      final User currentUser = new User();
      currentUser.setId("1");

      final UserContactInformation userContactInformation = new UserContactInformation();
      userContactInformation.setEmailWork("example@example.com");
      currentUser.setUserContactInformation(userContactInformation);

      final Company company = new Company();
      company.setId("1");
      this.currentUser = currentUser;
      targetUserId = "2";
      final User targetUser = new User();
      targetUser.setId(targetUserId);
      Mockito.when(userRepository.findActiveUserById(Mockito.anyString())).thenReturn(targetUser);
    }

    @Test
    void whenIsAdmin_thenShouldReturnTrue() {
      Mockito.when(userRepository.findActiveUserById(Mockito.anyString())).thenReturn(new User());
      final UserRole userRole = new UserRole();
      userRole.setName(Role.ADMIN.name());
      currentUser.setUserRole(userRole);
      final boolean hasAccess = userService.hasUserAccess(currentUser, targetUserId);
      Assertions.assertTrue(hasAccess);
    }

    @Test
    void whenIsManager_thenShouldReturnTrue() {
      final User targetUser = new User();
      targetUser.setManagerUser(currentUser);
      Mockito.when(userRepository.findActiveUserById(Mockito.anyString())).thenReturn(targetUser);
      final UserRole userRole = new UserRole();
      userRole.setName(Role.MANAGER.name());
      currentUser.setUserRole(userRole);
      final boolean hasAccess = userService.hasUserAccess(currentUser, targetUserId);
      Assertions.assertTrue(hasAccess);
    }

    @Test
    void whenIsNotManagerAndAdmin_thenShouldReturnFalse() {
      Mockito.when(userRepository.getManagerUserIdById(Mockito.anyString())).thenReturn(null);
      Mockito.when(auth0Helper.getUserRole(currentUser)).thenReturn(Role.EMPLOYEE);
      final boolean hasAccess = userService.hasUserAccess(currentUser, targetUserId);
      Assertions.assertFalse(hasAccess);
    }

    @Test
    void whenTargetUserIsNull_thenShouldThrow() {
      Mockito.when(userRepository.findActiveUserById(Mockito.anyString())).thenReturn(null);
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> userService.hasUserAccess(currentUser, targetUserId));
    }
  }

  @Nested
  class SendResetPasswordEmail {

    @Test
    void whenUserIsNull_thenShouldThrow() {
      Mockito.when(userRepository.findByEmailWork(Mockito.anyString())).thenReturn(new User());
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any())).thenReturn(null);
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> userService.sendResetPasswordEmail("example@indeed.com"));
    }

    @Test
    void whenNoError_thenShouldSuccess() {
      final com.auth0.json.mgmt.users.User auth0User = new com.auth0.json.mgmt.users.User();

      final User databaseUser = new User();
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any())).thenReturn(auth0User);
      Mockito.when(userRepository.findByEmailWork(Mockito.anyString())).thenReturn(databaseUser);
      Mockito.when(userRepository.findByEmailWork(Mockito.anyString())).thenReturn(new User());
      Assertions.assertDoesNotThrow(() -> userService.sendResetPasswordEmail("example@indeed.com"));
    }

    @Test
    void whenTargetUserIsNull_thenShouldThrow() {
      Mockito.when(userRepository.findByEmailWork(Mockito.anyString())).thenReturn(null);
      assertThatExceptionOfType(UserNotFoundByEmailException.class)
          .isThrownBy(() -> userService.sendResetPasswordEmail("example@indeed.com"));
    }
  }

  @Nested
  class createPasswordAndInvitationTokenExist {

    String passwordToken, invitationToken;
    User currentUser;

    @BeforeEach
    void setUp() {
      currentUser = new User();
      passwordToken = "a";
      invitationToken = "b";
      currentUser.setInvitationEmailToken(passwordToken);
      currentUser.setResetPasswordToken(invitationToken);
      currentUser.setUserStatus(new UserStatus("ACTIVE"));
      currentUser.setInvitedAt(Timestamp.from(Instant.now()));
    }

    @Test
    void whenUserIsNull_thenShouldThrow() {
      Mockito.when(userRepository.findByInvitationEmailToken(Mockito.any())).thenReturn(null);

      Assertions.assertThrows(
          UserNotFoundByInvitationTokenException.class,
          () -> userService.createPasswordAndInvitationTokenExist(passwordToken, invitationToken));
    }

    @Test
    void whenInvitationTokenExistPasswordNotExist_thenShouldReturnFalse() {
      Mockito.when(userRepository.findByInvitationEmailToken(Mockito.any()))
          .thenReturn(currentUser);
      Mockito.when(userRepository.existsByResetPasswordToken(Mockito.anyString()))
          .thenReturn(false);
      Assertions.assertFalse(
          userService.createPasswordAndInvitationTokenExist(passwordToken, invitationToken));
    }

    @Test
    void whenUserExistInvitedExpired_thenShouldThrow() {
      currentUser.setInvitedAt(Timestamp.valueOf(LocalDateTime.now().minus(100, ChronoUnit.HOURS)));
      Mockito.when(userRepository.findByInvitationEmailToken(Mockito.any()))
          .thenReturn(currentUser);
      Mockito.when(userRepository.existsByResetPasswordToken(Mockito.anyString())).thenReturn(true);
      Assertions.assertThrows(
          EmailExpiredException.class,
          () -> userService.createPasswordAndInvitationTokenExist(passwordToken, invitationToken));
    }

    @Test
    void whenUserExistInvitedNotExpired_thenShouldReturnTrue() {
      currentUser.setInvitedAt(Timestamp.valueOf(LocalDateTime.now().minus(1, ChronoUnit.HOURS)));
      Mockito.when(userRepository.findByInvitationEmailToken(Mockito.any()))
          .thenReturn(currentUser);
      Mockito.when(userRepository.existsByResetPasswordToken(Mockito.anyString())).thenReturn(true);
      Assertions.assertTrue(
          userService.createPasswordAndInvitationTokenExist(passwordToken, invitationToken));
    }
  }

  @Nested
  class issCurrentActiveAnnouncementDismissed {

    @Test
    void whenDismissAdIsNotNull_then_shouldReturnTrue() {
      final DismissedAt dismissedAt = new DismissedAt();

      Mockito.when(
              dismissedAtService.findByUserIdAndSystemAnnouncementId(Mockito.any(), Mockito.any()))
          .thenReturn(dismissedAt);

      Assertions.assertDoesNotThrow(
          () -> userService.isCurrentActiveAnnouncementDismissed(UuidUtil.getUuidString(), "1"));
      Assertions.assertTrue(
          userService.isCurrentActiveAnnouncementDismissed(UuidUtil.getUuidString(), "1"));
    }

    @Test
    void whenDismissAdIsNull_then_shouldReturnFalse() {
      Mockito.when(
              dismissedAtService.findByUserIdAndSystemAnnouncementId(Mockito.any(), Mockito.any()))
          .thenReturn(null);

      Assertions.assertDoesNotThrow(
          () -> userService.isCurrentActiveAnnouncementDismissed(UuidUtil.getUuidString(), "1"));
      Assertions.assertFalse(
          userService.isCurrentActiveAnnouncementDismissed(UuidUtil.getUuidString(), "1"));
    }
  }

  @Test
  void testCheckUserVerifiedEmail() {
    final com.auth0.json.mgmt.users.User auth0User = new com.auth0.json.mgmt.users.User();
    auth0User.setEmailVerified(true);
    Mockito.when(auth0Helper.findByEmail(Mockito.anyString())).thenReturn(auth0User);
    Assertions.assertTrue(userService.checkUserVerifiedEmail("example@example.com"));
  }

  @Test
  void testFindSuperUser() {
    final String companyId = RandomStringUtils.randomAlphabetic(16);
    final User user = new User();
    Mockito.when(userRepository.findSuperUser(companyId)).thenReturn(user);
    final User resultUser = userService.findSuperUser(companyId);
    assertThat(resultUser).isEqualTo(user);
  }

  @Test
  void testDeleteUser() {
    final com.auth0.json.mgmt.users.User auth0User = new com.auth0.json.mgmt.users.User();
    final User employee = new User();
    final User deletedUser = new User();
    final UserContactInformation userContactInformation = new UserContactInformation();
    employee.setId("1");
    userContactInformation.setEmailWork("@indeed.com");
    employee.setUserContactInformation(userContactInformation);
    Mockito.when(userRepository.findAllByManagerUserId(Mockito.anyString()))
        .thenReturn(Collections.emptyList());
    Mockito.when(entityManager.find(Mockito.any(), Mockito.any())).thenReturn(deletedUser);
    Mockito.when(
            auth0Helper.getAuth0UserByIdWithByEmailFailover(
                Mockito.anyString(), Mockito.anyString()))
        .thenReturn(auth0User);
    assertThatCode(() -> userService.deleteUser(employee)).doesNotThrowAnyException();
  }

  @Test
  void getUserNameInUsers() {
    final User user = new User();
    final List<User> users = new ArrayList<>();
    final UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
    userPersonalInformation.setFirstName("1");
    userPersonalInformation.setLastName("2");

    final UserContactInformation userContactInformation = new UserContactInformation();
    userContactInformation.setEmailWork("test@qq.com");

    user.setId(UuidUtil.getUuidString());
    user.setUserContactInformation(userContactInformation);
    user.setUserPersonalInformation(userPersonalInformation);

    users.add(user);

    final User newUser = new User();
    newUser.setId(UuidUtil.getUuidString());
    newUser.setUserContactInformation(userContactInformation);
    newUser.setUserPersonalInformation(userPersonalInformation);

    users.add(newUser);

    final String name = userService.getUserNameInUsers(user, users);
    assertThat(name)
        .isEqualTo(
            user.getUserPersonalInformation().getName()
                + " ("
                + userContactInformation.getEmailWork()
                + ")");
  }

  @Nested
  class TestSendResetPasswordEmail {
    String email = "qwe@asd.zxc";

    @Test
    void whenUserNotExist_thenShouldThrow() {
      Mockito.when(userRepository.findByEmailWork(email)).thenReturn(null);
      assertThatExceptionOfType(UserNotFoundByEmailException.class)
          .isThrownBy(() -> userService.sendResetPasswordEmail(email));
    }

    @Test
    void whenAuth0UserNotExist_thenShouldThrow() {
      final User user = new User();
      user.setId(UuidUtil.getUuidString());
      Mockito.when(userRepository.findByEmailWork(email)).thenReturn(user);
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(user.getId())).thenReturn(null);
      assertThatExceptionOfType(ResourceNotFoundException.class)
          .isThrownBy(() -> userService.sendResetPasswordEmail(email));
    }

    @Test
    void whenUserExist_auth0UserExist_thenShouldSuccess() {
      final User user = new User();
      user.setId(UuidUtil.getUuidString());
      Mockito.when(userRepository.findByEmailWork(email)).thenReturn(user);

      final com.auth0.json.mgmt.users.User auth0User = new com.auth0.json.mgmt.users.User();
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(user.getId())).thenReturn(auth0User);

      Mockito.when(emailService.getResetPasswordEmail(Mockito.anyString(), Mockito.anyString()))
          .thenReturn("");
      assertThatCode(() -> userService.sendResetPasswordEmail(email)).doesNotThrowAnyException();
    }
  }

  @Test
  void testFindRegisteredUsersByCompany() {
    final User user = new User();
    user.setId("1");
    final UserStatus userStatus = new UserStatus(Status.PENDING_VERIFICATION.name());
    user.setUserStatus(userStatus);
    final List<User> mockedUsers = Collections.singletonList(user);
    Mockito.when(userRepository.findAllActiveUsers()).thenReturn(mockedUsers);
    final List<User> users = userService.findRegisteredUsers();
    assertThat(users).isEmpty();
  }

  @Nested
  class AttendanceUser {
    String periodId = "test_period_id";
    User notApprovedUser = new User();
    Company company = new Company();
    String companyId = "test_company_id";

    @Test
    void whenPeriodIdValid_listNotSubmitTimeSheetUser_shouldSucceed() {
      Mockito.when(
              userRepository.findUsersByPeriodIdAndTimeSheetStatus(
                  periodId, StaticTimesheetStatus.TimeSheetStatus.ACTIVE.name()))
          .thenReturn(new ArrayList<>());
      assertThatCode(() -> userService.listMessageOnNotSubmitTimeSheetUsers(periodId))
          .doesNotThrowAnyException();
    }

    @Test
    void whenPeriodIdValid_listHasPendingTimeSheetsManagerAndAdmin_shouldSucceed() {
      company.setId(companyId);
      Mockito.when(
              userRepository.findUsersByPeriodIdAndTimeSheetStatus(
                  periodId, StaticTimesheetStatus.TimeSheetStatus.SUBMITTED.name()))
          .thenReturn(Arrays.asList(notApprovedUser));
      Mockito.when(
              userRepository.findManagersByPeriodIdAndTimeSheetStatus(
                  periodId, StaticTimesheetStatus.TimeSheetStatus.SUBMITTED.name()))
          .thenReturn(new ArrayList<>());
      Mockito.when(userRepository.findUsersByUserRole(Role.ADMIN.name()))
          .thenReturn(new ArrayList<>());
      assertThatCode(() -> userService.listHasPendingTimeSheetsManagerAndAdmin(periodId))
          .doesNotThrowAnyException();
    }
  }
}
