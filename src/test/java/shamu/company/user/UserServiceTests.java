package shamu.company.user;

import java.util.ArrayList;
import java.util.Collections;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
import shamu.company.authorization.PermissionUtils;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.service.DepartmentService;
import shamu.company.company.entity.Company;
import shamu.company.company.service.CompanyBenefitsSettingService;
import shamu.company.company.service.CompanyService;
import shamu.company.email.EmailService;
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
import shamu.company.server.dto.AuthUser;
import shamu.company.timeoff.service.PaidHolidayService;
import shamu.company.user.dto.ChangePasswordDto;
import shamu.company.user.dto.CreatePasswordDto;
import shamu.company.user.dto.CurrentUserDto;
import shamu.company.user.dto.UpdatePasswordDto;
import shamu.company.user.dto.UserRoleUpdateDto;
import shamu.company.user.dto.UserSignUpDto;
import shamu.company.user.dto.UserStatusUpdateDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.entity.mapper.UserAddressMapper;
import shamu.company.user.entity.mapper.UserContactInformationMapper;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.repository.UserRepository;
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
  @Mock private AwsHelper awsHelper;
  @Mock private UserRoleService userRoleService;
  @Mock private PermissionUtils permissionUtils;
  @Mock private CompanyBenefitsSettingService companyBenefitsSettingService;
  @Mock private UserBenefitsSettingService userBenefitsSettingService;


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
    final EmployeeListSearchCondition employeeListSearchCondition = new EmployeeListSearchCondition();
    final User currentUser = new User();
    currentUser.setCompany(new Company(UUID.randomUUID().toString().replaceAll("-", "")));
    Mockito.when(permissionUtils.hasAuthority(Mockito.anyString())).thenReturn(false);
    Mockito.when(userRepository.findActiveUserById(userId)).thenReturn(currentUser);
    Assertions.assertDoesNotThrow(() -> userService.findAllEmployees(userId, employeeListSearchCondition));
  }

  @Nested
  class testGetOrgChart {
    private String userId;
    private String companyId;

    @BeforeEach
    void init() {
      userId = UUID.randomUUID().toString().replaceAll("-", "");
      companyId = UUID.randomUUID().toString().replaceAll("-", "");
    }

    @Test
    void whenManagerIsNull_thenShouldThrow() {
      Mockito.when(userRepository.findOrgChartItemByUserId(userId, companyId)).thenReturn(null);
      Assertions.assertThrows(ForbiddenException.class, () -> userService.getOrgChart(userId, companyId));
    }

    @Test
    void whenManagerIsNotNull_thenShouldNotThrow() {
      final OrgChartDto manager = new OrgChartDto();
      final List<OrgChartDto> orgChartUserItemList = new ArrayList<>();
      manager.setId(UUID.randomUUID().toString().replaceAll("-", ""));
      orgChartUserItemList.add(manager);
      Mockito.when(userRepository.findOrgChartItemByUserId(userId, companyId))
          .thenReturn(manager);
      Mockito.when(userRepository.findOrgChartItemByManagerId(manager.getId(), companyId))
          .thenReturn(orgChartUserItemList);
      Assertions.assertDoesNotThrow(() -> userService.getOrgChart(userId, companyId));
    }

    @Test
    void whenUserIdIsNull_thenShouldCall() {
      userId = null;
      Mockito.when(userRepository.findOrgChartItemByManagerId(null, companyId))
          .thenReturn(new ArrayList<>());
      userService.getOrgChart(userId, companyId);
      Mockito.verify(userRepository, Mockito.times(1))
          .findOrgChartItemByManagerId(null, companyId);
    }
  }

  @Test
  void testGetPreSetAccountInfoByUserId() {
    final String userId = UUID.randomUUID().toString().replaceAll("-", "");
    final User user = new User();
    Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    Assertions.assertDoesNotThrow(() -> userService.getPreSetAccountInfoByUserId(userId));
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
      userStatusUpdateDto= new UserStatusUpdateDto();
      userStatusUpdateDto.setUserStatus(Status.ACTIVE);
      userStatusUpdateDto.setDeactivationReason(new SelectFieldInformationDto("id","name"));
      user = new User();
      final UserRole userRole = new UserRole();
      userId = UUID.randomUUID().toString().replaceAll("-", "");
      userRole.setName("test");
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
      Mockito.verify(dynamicScheduler, Mockito.times(1))
          .updateOrAddUniqueTriggerTask(Mockito.anyString(), Mockito.any(), Mockito.any());
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
      Assertions.assertThrows(ForbiddenException.class,
          () -> userService.updatePassword(changePasswordDto, userId));
    }

    @Test
    void whenPasswordIsError_thenShouldThrow() {
      Mockito.when(auth0Helper.isPasswordValid(user.getEmail(), changePasswordDto.getPassword()))
          .thenReturn(false);
      Assertions.assertThrows(ForbiddenException.class,
          () -> userService.updatePassword(changePasswordDto, userId));
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
  class UpdateWorkEmail {
    private String userId;
    private EmailUpdateDto emailUpdateDto;

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

      Mockito.when(auth0Helper.isPasswordValid(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(true);
    }

    @Test
    void whenPasswordIsNotValid_thenShouldThrow() {
      Mockito.when(auth0Helper.isPasswordValid(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(false);
      Assertions.assertThrows(ForbiddenException.class,
        () -> userService.updateWorkEmail(userId, emailUpdateDto));
    }

    @Test
    void whenEmailIsSame_thenShouldThrow() {
      emailUpdateDto.setEmail("example@example.com");
      Assertions.assertThrows(ForbiddenException.class,
          () -> userService.updateWorkEmail(userId, emailUpdateDto));
    }

    @Test
    void whenNewEmailIsUsed_thenShouldThrow() {
      Mockito.when(auth0Helper.existsByEmail(emailUpdateDto.getEmail())).thenReturn(true);
      Assertions.assertThrows(ForbiddenException.class,
          () -> userService.updateWorkEmail(userId, emailUpdateDto));
    }

    @Test
    void whenOk_thenShouldSendEmail() {
      Mockito.when(auth0Helper.existsByEmail(emailUpdateDto.getEmail())).thenReturn(false);
      userService.updateWorkEmail(userId, emailUpdateDto);

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
    private String id;
    MultipartFile file;

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
    private String email;
    private final com.auth0.json.mgmt.users.User auth0User = new com.auth0.json.mgmt.users.User();

    @Test
    void whenUserIsNull_thenShouldThrow() {
      Mockito.when(auth0Helper.findByEmail(email)).thenReturn(null);
      Assertions.assertThrows(ForbiddenException.class,
          () -> userService.resendVerificationEmail(email));
    }

    @Test
    void whenUserIsVerified_thenShouldThrow() {
      auth0User.setEmailVerified(true);
      Mockito.when(auth0Helper.findByEmail(email)).thenReturn(auth0User);
      Assertions.assertThrows(ForbiddenException.class,
          () -> userService.resendVerificationEmail(email));
    }

    @Test
    void whenOk_thenShouldSendEmail() {
      auth0User.setEmailVerified(false);
      auth0User.setId(UUID.randomUUID().toString().replaceAll("-", ""));
      Mockito.when(auth0Helper.findByEmail(email)).thenReturn(auth0User);
      userService.resendVerificationEmail(email);

      Mockito.verify(auth0Helper, Mockito.times(1))
          .sendVerificationEmail(Mockito.anyString());
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
              .userId(userId)
              .companyName(RandomStringUtils.randomAlphabetic(4))
              .firstName(RandomStringUtils.randomAlphabetic(3))
              .lastName(RandomStringUtils.randomAlphabetic(3))
              .phone(RandomStringUtils.randomAlphabetic(11))
              .build();
    }

    @Test
    void whenCanNotFindEmail_thenShouldThrow() {
      Assertions.assertThrows(ForbiddenException.class, () -> userService.signUp(userSignUpDto));
    }

    @Test
    void whenCanFindEmail_thenShouldSuccess() {
      final com.auth0.json.mgmt.users.User auth0User = new com.auth0.json.mgmt.users.User();
      auth0User.setEmail("example@mail.com");
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(userId)).thenReturn(auth0User);
      final Company company = new Company();
      company.setName("company");
      Mockito.when(companyService.save(Mockito.any())).thenReturn(company);
      Mockito.when(userStatusService.findByName(Mockito.any()))
          .thenReturn(new UserStatus(Status.ACTIVE.name()));
      Mockito.when(userRepository.save(Mockito.any())).thenReturn(new User());

      userService.signUp(userSignUpDto);
      Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
    }
  }

  @Test
  void testGetCurrentUserInfo() {
    final User currentUser = new User();
    currentUser.setId("1");
    final String userId = UUID.randomUUID().toString().replaceAll("-", "");
    currentUser.setId(userId);

    final UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
    userPersonalInformation.setFirstName("Aa");
    userPersonalInformation.setLastName("Bb");
    currentUser.setUserPersonalInformation(userPersonalInformation);

    currentUser.setImageUrl(RandomStringUtils.randomAlphabetic(11));

    final Company company = new Company();
    company.setId("1");

    Mockito.when(userRepository.findById(Mockito.anyString()))
        .thenReturn(java.util.Optional.of(currentUser));
    Mockito.when(userRepository.findByManagerUser(Mockito.any()))
        .thenReturn(Collections.emptyList());

    final CurrentUserDto userInfo = userService.getCurrentUserInfo(currentUser.getId());
    Assertions.assertEquals(userInfo.getId(), currentUser.getId());
  }

  @Test
  void testResetPassword() {
    final UpdatePasswordDto updatePasswordDto = new UpdatePasswordDto();
    updatePasswordDto.setNewPassword(RandomStringUtils.randomAlphabetic(10));
    updatePasswordDto.setResetPasswordToken(RandomStringUtils.randomAlphabetic(10));

    final User databaseUser = new User();
    final UserContactInformation userContactInformation = new UserContactInformation();
    userContactInformation.setEmailWork("example@indeed.com");
    databaseUser.setUserContactInformation(userContactInformation);

    final com.auth0.json.mgmt.users.User authUser = new com.auth0.json.mgmt.users.User();

    Mockito.when(userRepository.findByResetPasswordToken(updatePasswordDto.getResetPasswordToken()))
        .thenReturn(databaseUser);
    Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any())).thenReturn(authUser);

    Assertions.assertDoesNotThrow(() -> userService.resetPassword(updatePasswordDto));
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
      currentUser.setCompany(company);
      this.currentUser = currentUser;
      targetUserId = "2";
      final User targetUser = new User();
      targetUser.setId(targetUserId);
      Mockito.when(userRepository.findByIdAndCompanyId(Mockito.anyString(), Mockito.anyString()))
          .thenReturn(targetUser);
    }

    @Test
    void whenIsAdmin_thenShouldReturnTrue() {
      Mockito.when(userRepository.findByIdAndCompanyId(Mockito.anyString(), Mockito.anyString()))
          .thenReturn(new User());
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
      Mockito.when(userRepository.findByIdAndCompanyId(Mockito.anyString(), Mockito.anyString()))
          .thenReturn(targetUser);
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
  }

  @Nested
  class CreatePassword {

    private CreatePasswordDto createPasswordDto;

    private com.auth0.json.mgmt.users.User user;

    @BeforeEach
    void setUp() {
      createPasswordDto = new CreatePasswordDto();
      createPasswordDto.setEmailWork("example@indeed.com");
      final String password =
          RandomStringUtils.randomAlphabetic(4).toUpperCase()
              + RandomStringUtils.randomAlphabetic(4).toLowerCase()
              + RandomStringUtils.randomNumeric(4);
      final String resetPasswordToken = UUID.randomUUID().toString().replaceAll("-", "");
      createPasswordDto.setNewPassword(password);
      createPasswordDto.setResetPasswordToken(resetPasswordToken);
      user = new com.auth0.json.mgmt.users.User();
    }

    @Test
    void whenPasswordTokenNotMatch_thenShouldThrow() {
      final User targetUser = new User();
      targetUser.setResetPasswordToken(RandomStringUtils.randomAlphabetic(10));
      Mockito.when(userRepository.findByEmailWork(Mockito.anyString())).thenReturn(targetUser);
      Assertions.assertThrows(
          ResourceNotFoundException.class, () -> userService.createPassword(createPasswordDto));
    }

    @Test
    void whenNoSuchUserInAuth0_thenShouldNotThrow() {
      final User targetUser = new User();
      targetUser.setResetPasswordToken(createPasswordDto.getResetPasswordToken());
      Mockito.when(userRepository.findByEmailWork(Mockito.anyString())).thenReturn(targetUser);
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any()))
          .thenReturn(new com.auth0.json.mgmt.users.User());
      Assertions.assertDoesNotThrow(() -> userService.createPassword(createPasswordDto));
    }

    @Test
    void whenNoSuchUserInDatabase_thenShouldThrow() {
      final User targetUser = new User();
      targetUser.setResetPasswordToken(createPasswordDto.getResetPasswordToken());
      Mockito.when(userRepository.findByEmailWork(Mockito.anyString())).thenReturn(null);
      Assertions.assertThrows(
          ResourceNotFoundException.class, () -> userService.createPassword(createPasswordDto));
    }

    @Test
    void whenResetTokenNotEqual_thenShouldThrow() {
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any())).thenReturn(user);
      final User targetUser = new User();
      targetUser.setResetPasswordToken(RandomStringUtils.randomAlphabetic(10));
      Mockito.when(userRepository.findByEmailWork(Mockito.any())).thenReturn(targetUser);
      Assertions.assertThrows(
          ResourceNotFoundException.class, () -> userService.createPassword(createPasswordDto));
    }

    @Test
    void whenNoError_thenShouldSuccess() {
      final User targetUser = new User();
      targetUser.setResetPasswordToken(createPasswordDto.getResetPasswordToken());
      Mockito.when(userRepository.findByEmailWork(Mockito.anyString())).thenReturn(targetUser);
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any())).thenReturn(user);
      final UserStatus targetStatus = new UserStatus();
      targetStatus.setName(Status.ACTIVE.name());
      Mockito.when(userStatusService.findByName(Mockito.any())).thenReturn(targetStatus);

      Assertions.assertDoesNotThrow(() -> userService.createPassword(createPasswordDto));
    }
  }

  @Nested
  class SendResetPasswordEmail {

    @Test
    void whenUserIsNull_thenShouldThrow() {
      Mockito.when(userRepository.findByEmailWork(Mockito.anyString())).thenReturn(new User());
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any())).thenReturn(null);
      Assertions.assertThrows(
          ForbiddenException.class, () -> userService.sendResetPasswordEmail("example@indeed.com"));
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
  }
}
