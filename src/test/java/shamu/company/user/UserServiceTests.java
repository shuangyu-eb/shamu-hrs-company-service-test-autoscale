package shamu.company.user;

import java.util.Collections;
import java.util.UUID;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.TaskScheduler;
import org.thymeleaf.ITemplateEngine;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.common.repository.DepartmentRepository;
import shamu.company.company.CompanyRepository;
import shamu.company.company.CompanySizeRepository;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.CompanySize;
import shamu.company.email.EmailService;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.job.repository.JobRepository;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.redis.AuthUserCacheManager;
import shamu.company.timeoff.service.PaidHolidayService;
import shamu.company.user.dto.CreatePasswordDto;
import shamu.company.user.dto.CurrentUserDto;
import shamu.company.user.dto.UpdatePasswordDto;
import shamu.company.user.dto.UserSignUpDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.entity.mapper.UserAddressMapper;
import shamu.company.user.entity.mapper.UserContactInformationMapper;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.repository.UserAccessLevelEventRepository;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.repository.UserStatusRepository;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserService;
import shamu.company.user.service.impl.UserServiceImpl;
import shamu.company.utils.Auth0Util;

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
  private Auth0Util auth0Util;
  @Mock
  private UserAccessLevelEventRepository userAccessLevelEventRepository;
  @Mock
  private TaskScheduler taskScheduler;
  @Mock
  private DepartmentRepository departmentRepository;
  @Mock
  private JobRepository jobRepository;
  @Mock
  private UserMapper userMapper;
  @Mock
  private AuthUserCacheManager authUserCacheManager;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    userService = new UserServiceImpl(templateEngine,
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
        auth0Util,
        userAccessLevelEventRepository,
        taskScheduler,
        departmentRepository,
        jobRepository, userMapper, authUserCacheManager);
  }

  @Test
  void testSignUp() {
    final String userId = UUID.randomUUID().toString().replaceAll("-", "");
    final UserSignUpDto userSignUpDto = UserSignUpDto.builder()
        .userId(userId)
        .companyName(RandomStringUtils.randomAlphabetic(4))
        .companySize("1-10")
        .firstName(RandomStringUtils.randomAlphabetic(3))
        .lastName(RandomStringUtils.randomAlphabetic(3))
        .phone(RandomStringUtils.randomAlphabetic(11))
        .build();

    Mockito.when(companySizeRepository.findCompanySizeByName(Mockito.anyString()))
        .thenReturn(new CompanySize());

    final Company company = new Company();
    company.setName("company");
    company.setCompanySize(new CompanySize(userSignUpDto.getCompanySize()));
    Mockito.when(companyRepository.save(Mockito.any())).thenReturn(company);

    Mockito.when(userStatusRepository.findByName(Mockito.anyString()))
        .thenReturn(new UserStatus(Status.ACTIVE.name()));

    Mockito.when(userRepository.save(Mockito.any())).thenReturn(new User());

    userService.signUp(userSignUpDto);

    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
  }

  @Test
  void testGetCurrentUserInfo() {
    final User currentUser = new User();
    currentUser.setId(1L);
    final String userId = UUID.randomUUID().toString().replaceAll("-", "");
    currentUser.setUserId(userId);

    final UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
    userPersonalInformation.setFirstName("Aa");
    userPersonalInformation.setLastName("Bb");
    currentUser.setUserPersonalInformation(userPersonalInformation);

    currentUser.setImageUrl(RandomStringUtils.randomAlphabetic(11));

    final Company company = new Company();
    company.setId(1L);

    Mockito.when(userRepository.findByUserId(Mockito.anyString())).thenReturn(currentUser);
    Mockito.when(userRepository.findByManagerUser(Mockito.any()))
        .thenReturn(Collections.emptyList());

    final CurrentUserDto userInfo = userService
        .getCurrentUserInfo(currentUser.getUserId());
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
    Mockito.when(auth0Util.getUserByEmailFromAuth0(userContactInformation.getEmailWork()))
        .thenReturn(authUser);

    Assertions.assertDoesNotThrow(() -> {
      userService.resetPassword(updatePasswordDto);
    });
  }

  @Nested
  class HasUserAccess {

    User currentUser;

    Long targetUserId;

    @BeforeEach
    void setUp() {
      final User currentUser = new User();
      currentUser.setId(1L);

      final UserContactInformation userContactInformation = new UserContactInformation();
      userContactInformation.setEmailWork("example@example.com");
      currentUser.setUserContactInformation(userContactInformation);

      final Company company = new Company();
      company.setId(1L);

      currentUser.setCompany(company);
      this.currentUser = currentUser;
      this.targetUserId = 2L;

      final User targetUser = new User();
      targetUser.setId(targetUserId);

      Mockito.when(userRepository.findByIdAndCompanyId(Mockito.anyLong(), Mockito.anyLong()))
          .thenReturn(targetUser);
    }


    @Test
    void whenIsAdmin_thenShouldReturnTrue() {
      Mockito.when(userRepository.findByIdAndCompanyId(Mockito.anyLong(), Mockito.anyLong()))
          .thenReturn(new User());
      Mockito.when(auth0Util.getUserRole(Mockito.anyString())).thenReturn(Role.ADMIN);
      final boolean hasAccess = userService.hasUserAccess(currentUser, targetUserId);
      Assertions.assertTrue(hasAccess);
    }

    @Test
    void whenIsManager_thenShouldReturnTrue() {
      final User targetUser = new User();
      targetUser.setManagerUser(currentUser);

      Mockito.when(userRepository.findByIdAndCompanyId(Mockito.anyLong(), Mockito.anyLong()))
          .thenReturn(targetUser);
      Mockito.when(userRepository.getManagerUserIdById(Mockito.anyLong()))
          .thenReturn(currentUser.getId());

      Mockito.when(auth0Util.getUserRole(Mockito.anyString())).thenReturn(Role.MANAGER);
      final boolean hasAccess = userService.hasUserAccess(currentUser, targetUserId);
      Assertions.assertTrue(hasAccess);
    }

    @Test
    void whenIsNotManagerAndAdmin_thenShouldReturnFalse() {
      Mockito.when(userRepository.getManagerUserIdById(Mockito.anyLong()))
          .thenReturn(null);
      Mockito.when(auth0Util.getUserRole(Mockito.anyString())).thenReturn(Role.EMPLOYEE);

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
    void whenPasswordNotMatch_thenShouldThrow() {
      createPasswordDto.setNewPassword(RandomStringUtils.randomAlphabetic(10));
      Assertions.assertThrows(ForbiddenException.class, () -> {
        userService.createPassword(createPasswordDto);
      });
    }

    @Test
    void whenNoSuchUserInAuth0_thenShouldThrow() {
      Mockito.when(auth0Util.getUserByEmailFromAuth0(Mockito.anyString())).thenReturn(null);
      Assertions.assertThrows(ForbiddenException.class, () -> {
        userService.createPassword(createPasswordDto);
      });
    }

    @Test
    void whenNoSuchUserInDatabase_thenShouldThrow() {
      Mockito.when(auth0Util.getUserByEmailFromAuth0(Mockito.anyString())).thenReturn(user);
      Mockito.when(auth0Util.getUserId(Mockito.any()))
          .thenReturn(RandomStringUtils.randomAlphabetic(10));
      Mockito.when(userRepository.findByUserId(Mockito.anyString())).thenReturn(null);
      Assertions.assertThrows(ResourceNotFoundException.class, () -> {
        userService.createPassword(createPasswordDto);
      });
    }

    @Test
    void whenResetTokenNotEqual_thenShouldThrow() {
      Mockito.when(auth0Util.getUserByEmailFromAuth0(Mockito.anyString())).thenReturn(user);
      Mockito.when(auth0Util.getUserId(Mockito.any()))
          .thenReturn(RandomStringUtils.randomAlphabetic(10));

      final User targetUser = new User();
      targetUser.setResetPasswordToken(RandomStringUtils.randomAlphabetic(10));
      Mockito.when(userRepository.findByUserId(Mockito.anyString())).thenReturn(targetUser);
      Assertions.assertThrows(ResourceNotFoundException.class, () -> {
        userService.createPassword(createPasswordDto);
      });
    }

    @Test
    void whenNoError_thenShouldSuccess() {
      Mockito.when(auth0Util.getUserByEmailFromAuth0(Mockito.anyString())).thenReturn(user);
      Mockito.when(auth0Util.getUserId(Mockito.any()))
          .thenReturn(RandomStringUtils.randomAlphabetic(10));
      final User targetUser = new User();
      targetUser.setResetPasswordToken(createPasswordDto.getResetPasswordToken());
      Mockito.when(userRepository.findByUserId(Mockito.anyString())).thenReturn(targetUser);

      final UserStatus targetStatus = new UserStatus();
      targetStatus.setName(Status.ACTIVE.name());
      Mockito.when(userStatusRepository.findByName(Mockito.anyString())).thenReturn(targetStatus);

      Assertions.assertDoesNotThrow(() -> {
        userService.createPassword(createPasswordDto);
      });
    }
  }

  @Nested
  class SendResetPasswordEmail {

    @Test
    void whenUserIsNull_thenShouldThrow() {
      Mockito.when(auth0Util.getUserByEmailFromAuth0(Mockito.anyString())).thenReturn(null);
      Assertions.assertThrows(ForbiddenException.class, () -> {
        userService.sendResetPasswordEmail("example@indeed.com");
      });
    }

    @Test
    void whenNoError_thenShouldSuccess() {
      final com.auth0.json.mgmt.users.User auth0User = new com.auth0.json.mgmt.users.User();

      final User databaseUser = new User();
      Mockito.when(auth0Util.getUserByEmailFromAuth0(Mockito.anyString())).thenReturn(auth0User);
      Mockito.when(userRepository.findByEmailWork(Mockito.anyString())).thenReturn(databaseUser);

      Assertions.assertDoesNotThrow(() -> {
        userService.sendResetPasswordEmail("example@indeed.com");
      });
    }
  }
}
