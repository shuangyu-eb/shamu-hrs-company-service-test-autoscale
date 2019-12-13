package shamu.company.user;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.thymeleaf.ITemplateEngine;
import shamu.company.authorization.PermissionUtils;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.DepartmentRepository;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.CompanySize;
import shamu.company.company.entity.mapper.*;
import shamu.company.company.repository.CompanyRepository;
import shamu.company.company.repository.CompanySizeRepository;
import shamu.company.email.EmailService;
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.employee.dto.JobInformationDto;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.entity.mapper.JobUserMapperImpl;
import shamu.company.job.repository.JobRepository;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.job.service.JobUserService;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.s3.AwsUtil;
import shamu.company.scheduler.DynamicScheduler;
import shamu.company.timeoff.service.PaidHolidayService;
import shamu.company.user.dto.CreatePasswordDto;
import shamu.company.user.dto.CurrentUserDto;
import shamu.company.user.dto.UpdatePasswordDto;
import shamu.company.user.dto.UserSignUpDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.entity.mapper.*;
import shamu.company.user.repository.UserAccessLevelEventRepository;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.user.repository.UserContactInformationRepository;
import shamu.company.user.repository.UserPersonalInformationRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.repository.UserStatusRepository;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserRoleService;
import shamu.company.user.service.UserService;

class UserServiceTests {

  private static UserService userService;

  @Mock
  private ITemplateEngine templateEngine;
  @Mock
  private UserRepository userRepository;
  @Mock
  private JobUserRepository jobUserRepository;
  @Mock
  private UserStatusRepository userStatusRepository;
  @Mock
  private EmailService emailService;
  @Mock
  private UserCompensationRepository userCompensationRepository;
  @Mock
  private UserPersonalInformationMapper userPersonalInformationMapper;
  @Mock
  private UserEmergencyContactService userEmergencyContactService;
  @Mock
  private UserAddressService userAddressService;
  @Mock
  private CompanySizeRepository companySizeRepository;
  @Mock
  private PaidHolidayService paidHolidayService;
  @Mock
  private CompanyRepository companyRepository;
  @Mock
  private UserContactInformationMapper userContactInformationMapper;
  @Mock
  private UserAddressMapper userAddressMapper;
  @Mock
  private Auth0Helper auth0Helper;
  @Mock
  private UserAccessLevelEventRepository userAccessLevelEventRepository;
  @Mock
  private DepartmentRepository departmentRepository;
  @Mock
  private JobRepository jobRepository;
  private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);
  @Mock
  private UserContactInformationRepository userContactInformationRepository;
  @Mock
  private UserPersonalInformationRepository userPersonalInformationRepository;
  @Mock
  private AuthUserCacheManager authUserCacheManager;
  @Mock
  private DynamicScheduler dynamicScheduler;
  @Mock
  private AwsUtil awsUtil;
  @Mock
  private UserRoleService userRoleService;
  @Mock
  private PermissionUtils permissionUtils;
  @Mock
  private JobUserService jobUserService;

  private final StateProvinceMapper stateProvinceMapper = Mappers
          .getMapper(StateProvinceMapper.class);
  private final OfficeAddressMapper officeAddressMapper = new OfficeAddressMapperImpl(
          stateProvinceMapper);
  private final OfficeMapper officeMapper = new OfficeMapperImpl(officeAddressMapper);
  private final UserCompensationMapper userCompensationMapper = Mappers
          .getMapper(UserCompensationMapper.class);
  private final JobUserMapper jobUserMapper =
          new JobUserMapperImpl(officeMapper, userCompensationMapper);

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    userService = new UserService(templateEngine,
        userRepository,
        jobUserRepository,
        userStatusRepository,
        emailService,
        userCompensationRepository,
        userPersonalInformationMapper,
        userEmergencyContactService,
        userAddressService,
        companySizeRepository,
        paidHolidayService,
        companyRepository,
        userContactInformationMapper,
        userAddressMapper,
        auth0Helper,
        userAccessLevelEventRepository,
        departmentRepository,
        jobRepository, userMapper, authUserCacheManager,userContactInformationRepository,
        userPersonalInformationRepository,
        dynamicScheduler,
        awsUtil, userRoleService, permissionUtils,
        jobUserService, jobUserMapper);
  }

  @Test
  void testSignUp() {
    final String userId = UUID.randomUUID().toString().replaceAll("-", "");
    final UserSignUpDto userSignUpDto = UserSignUpDto.builder()
        .userId(userId)
        .companyName(RandomStringUtils.randomAlphabetic(4))
        .companySizeId(UUID.randomUUID().toString())
        .firstName(RandomStringUtils.randomAlphabetic(3))
        .lastName(RandomStringUtils.randomAlphabetic(3))
        .phone(RandomStringUtils.randomAlphabetic(11))
        .build();

    Mockito.when(companySizeRepository.findById(Mockito.anyString()))
        .thenReturn(Optional.of(new CompanySize()));

    final Company company = new Company();
    company.setName("company");
    company.setCompanySize(new CompanySize(userSignUpDto.getCompanySizeId()));
    Mockito.when(companyRepository.save(Mockito.any())).thenReturn(company);

    Mockito.when(userStatusRepository.findByName(Mockito.any()))
        .thenReturn(new UserStatus(Status.ACTIVE.name()));

    Mockito.when(userRepository.save(Mockito.any())).thenReturn(new User());

    userService.signUp(userSignUpDto);

    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
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

    Mockito.when(userRepository.findByUserId(Mockito.anyString()))
            .thenReturn(currentUser);
    Mockito.when(userRepository.findByManagerUser(Mockito.any()))
        .thenReturn(Collections.emptyList());

    final CurrentUserDto userInfo = userService
        .getCurrentUserInfo(currentUser.getId());
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
    Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any()))
        .thenReturn(authUser);

    Assertions.assertDoesNotThrow(() -> {
      userService.resetPassword(updatePasswordDto);
    });
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
      Mockito.when(userRepository.getManagerUserIdById(Mockito.anyString()))
          .thenReturn(null);
      Mockito.when(auth0Helper.getUserRole(Mockito.anyString())).thenReturn(Role.EMPLOYEE);

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

      final String password = RandomStringUtils.randomAlphabetic(4).toUpperCase()
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
      Assertions.assertThrows(ResourceNotFoundException.class, () -> {
        userService.createPassword(createPasswordDto);
      });
    }

    @Test
    void whenNoSuchUserInAuth0_thenShouldThrow() {
      final User targetUser = new User();
      targetUser.setResetPasswordToken(createPasswordDto.getResetPasswordToken());
      Mockito.when(userRepository.findByEmailWork(Mockito.anyString())).thenReturn(targetUser);
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any())).thenReturn(null);
      Assertions.assertThrows(ForbiddenException.class, () -> {
        userService.createPassword(createPasswordDto);
      });
    }

    @Test
    void whenNoSuchUserInDatabase_thenShouldThrow() {
      final User targetUser = new User();
      targetUser.setResetPasswordToken(createPasswordDto.getResetPasswordToken());
      Mockito.when(userRepository.findByEmailWork(Mockito.anyString())).thenReturn(null);
      Assertions.assertThrows(ResourceNotFoundException.class, () -> {
        userService.createPassword(createPasswordDto);
      });
    }

    @Test
    void whenResetTokenNotEqual_thenShouldThrow() {
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any())).thenReturn(user);
      final User targetUser = new User();
      targetUser.setResetPasswordToken(RandomStringUtils.randomAlphabetic(10));
      Mockito.when(userRepository.findByEmailWork(Mockito.any())).thenReturn(targetUser);
      Assertions.assertThrows(ResourceNotFoundException.class, () -> {
        userService.createPassword(createPasswordDto);
      });
    }

    @Test
    void whenNoError_thenShouldSuccess() {
      final User targetUser = new User();
      targetUser.setResetPasswordToken(createPasswordDto.getResetPasswordToken());
      Mockito.when(userRepository.findByEmailWork(Mockito.anyString())).thenReturn(targetUser);

      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any())).thenReturn(user);

      final UserStatus targetStatus = new UserStatus();
      targetStatus.setName(Status.ACTIVE.name());
      Mockito.when(userStatusRepository.findByName(Mockito.any())).thenReturn(targetStatus);

      Assertions.assertDoesNotThrow(() -> {
        userService.createPassword(createPasswordDto);
      });
    }
  }

  @Nested
  class SendResetPasswordEmail {

    @Test
    void whenUserIsNull_thenShouldThrow() {
      Mockito.when(userRepository.findByEmailWork(Mockito.anyString())).thenReturn(new User());
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any())).thenReturn(null);
      Assertions.assertThrows(ForbiddenException.class, () -> {
        userService.sendResetPasswordEmail("example@indeed.com");
      });
    }

    @Test
    void whenNoError_thenShouldSuccess() {
      final com.auth0.json.mgmt.users.User auth0User = new com.auth0.json.mgmt.users.User();

      final User databaseUser = new User();
      Mockito.when(auth0Helper.getUserByUserIdFromAuth0(Mockito.any())).thenReturn(auth0User);
      Mockito.when(userRepository.findByEmailWork(Mockito.anyString())).thenReturn(databaseUser);

      Mockito.when(userRepository.findByEmailWork(Mockito.anyString())).thenReturn(new User());

      Assertions.assertDoesNotThrow(() -> {
        userService.sendResetPasswordEmail("example@indeed.com");
      });
    }
  }

  @Nested
  class FindJobMessage {

    private String targetUserId;

    private String userId;

    private JobUser jobUser;

    private User currentUser;

    @BeforeEach
    void init() {
      jobUser = new JobUser();
      final User targetUser = new User();
      targetUserId = RandomStringUtils.randomAlphabetic(16);
      targetUser.setId(targetUserId);
      final UserRole userRole = new UserRole();
      userRole.setName(Role.MANAGER.name());
      targetUser.setUserRole(userRole);
      jobUser.setUser(targetUser);
      Optional<User> optionalTargetUser = Optional.ofNullable(targetUser);
      Mockito.when(jobUserService.getJobUserByUserId(Mockito.anyString())).thenReturn(jobUser);
      Mockito.when(userRepository.findById(targetUserId)).thenReturn(optionalTargetUser);

      currentUser = new User();
      userId = RandomStringUtils.randomAlphabetic(16);
      currentUser.setId(userId);
      final UserRole currentUserRole = new UserRole();
      currentUserRole.setName(Role.MANAGER.name());
      currentUser.setUserRole(currentUserRole);
      Optional<User> optionalCurrentUser = Optional.ofNullable(currentUser);
      Mockito.when(userRepository.findById(userId)).thenReturn(optionalCurrentUser);
    }

    @Test
    void whenCanNotFindUserJob_thenReturnBasicJobInformation() {
      Mockito.when(jobUserService.getJobUserByUserId(Mockito.anyString())).thenReturn(null);

      final BasicJobInformationDto jobInformation = userService
              .findJobMessage(targetUserId, userId);
      Assertions.assertNotNull(jobInformation);
    }

    @Test
    void whenIsCurrentUser_thenReturnJobInformation() {
      final BasicJobInformationDto jobInformation = userService
              .findJobMessage(targetUserId, targetUserId);
      Assertions.assertTrue(jobInformation instanceof JobInformationDto);
    }

    @Test
    void whenIsAdmin_thenReturnJobInformation() {
      final UserRole adminRole = new UserRole();
      adminRole.setName(Role.ADMIN.name());
      currentUser.setUserRole(adminRole);
      final BasicJobInformationDto jobInformation = userService
              .findJobMessage(targetUserId, userId);
      Assertions.assertTrue(jobInformation instanceof JobInformationDto);
    }

    @Test
    void whenIsUserManager_thenReturnJobInformation() {
      jobUser.getUser().setManagerUser(currentUser);
      final BasicJobInformationDto jobInformation = userService
              .findJobMessage(targetUserId, userId);
      Assertions.assertTrue(jobInformation instanceof JobInformationDto);
    }

    @Test
    void whenIsEmployee_thenReturnBasicJobInformation() {
      final User randomManagerUser = new User();
      jobUser.getUser().setManagerUser(currentUser);
      randomManagerUser.setId(RandomStringUtils.randomAlphabetic(16));
      final BasicJobInformationDto jobInformation = userService
              .findJobMessage(targetUserId, userId);
      Assertions.assertNotNull(jobInformation);
    }
  }
}
