package shamu.company.employee;

import java.util.Base64;
import java.util.UUID;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.context.ApplicationEventPublisher;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.common.service.CountryService;
import shamu.company.common.service.OfficeService;
import shamu.company.common.service.StateProvinceService;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.mapper.OfficeAddressMapper;
import shamu.company.company.entity.mapper.OfficeAddressMapperImpl;
import shamu.company.company.entity.mapper.OfficeMapper;
import shamu.company.company.entity.mapper.OfficeMapperImpl;
import shamu.company.company.entity.mapper.StateProvinceMapper;
import shamu.company.crypto.EncryptorUtil;
import shamu.company.email.Email;
import shamu.company.email.EmailService;
import shamu.company.employee.dto.EmailResendDto;
import shamu.company.employee.dto.EmployeeContactInformationDto;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.dto.EmployeePersonalInformationDto;
import shamu.company.employee.dto.UserPersonalInformationForManagerDto;
import shamu.company.employee.service.EmployeeService;
import shamu.company.employee.service.EmploymentTypeService;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.info.entity.mapper.UserEmergencyContactMapper;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.entity.mapper.JobUserMapperImpl;
import shamu.company.job.service.JobService;
import shamu.company.job.service.JobUserService;
import shamu.company.s3.AwsUtil;
import shamu.company.user.dto.BasicUserContactInformationDto;
import shamu.company.user.dto.BasicUserPersonalInformationDto;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.entity.mapper.UserAddressMapper;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.entity.mapper.UserContactInformationMapper;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.service.CompensationFrequencyService;
import shamu.company.user.service.GenderService;
import shamu.company.user.service.MaritalStatusService;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserCompensationService;
import shamu.company.user.service.UserContactInformationService;
import shamu.company.user.service.UserPersonalInformationService;
import shamu.company.user.service.UserRoleService;
import shamu.company.user.service.UserService;
import shamu.company.user.service.UserStatusService;
import shamu.company.utils.FileValidateUtil.FileType;

class EmployeeServiceTests {

  @Mock
  private JobUserService jobUserService;
  @Mock
  private UserAddressService userAddressService;
  @Mock
  private EmploymentTypeService employmentTypeService;
  @Mock
  private UserCompensationService userCompensationService;
  @Mock
  private JobService jobService;
  @Mock
  private GenderService genderService;
  @Mock
  private MaritalStatusService maritalStatusService;
  @Mock private AwsUtil awsUtil;
  @Mock
  private CompensationFrequencyService compensationFrequencyService;
  @Mock
  private StateProvinceService stateProvinceService;
  @Mock
  private CountryService countryService;
  @Mock
  private UserEmergencyContactService userEmergencyContactService;
  @Mock private UserService userService;
  @Mock
  private UserStatusService userStatusService;
  @Mock private EmailService emailService;
  @Mock
  private OfficeService officeService;
  @Mock private UserPersonalInformationService userPersonalInformationService;
  @Mock private UserContactInformationService userContactInformationService;
  private final UserPersonalInformationMapper userPersonalInformationMapper = Mappers
      .getMapper(UserPersonalInformationMapper.class);
  private final UserAddressMapper userAddressMapper = Mappers.getMapper(UserAddressMapper.class);
  private final UserContactInformationMapper userContactInformationMapper = Mappers
      .getMapper(UserContactInformationMapper.class);
  private final UserEmergencyContactMapper userEmergencyContactMapper = Mappers
      .getMapper(UserEmergencyContactMapper.class);
  @Mock
  private Auth0Helper auth0Helper;
  @Mock private ApplicationEventPublisher applicationEventPublisher;

  private final StateProvinceMapper stateProvinceMapper = Mappers
      .getMapper(StateProvinceMapper.class);
  private final OfficeAddressMapper officeAddressMapper = new OfficeAddressMapperImpl(
      stateProvinceMapper);
  private final OfficeMapper officeMapper = new OfficeMapperImpl(officeAddressMapper);
  private final UserCompensationMapper userCompensationMapper = Mappers
      .getMapper(UserCompensationMapper.class);
  private final JobUserMapper jobUserMapper =
      new JobUserMapperImpl(officeMapper, userCompensationMapper);
  @Mock private UserRoleService userRoleService;
  @Mock
  private EncryptorUtil encryptorUtil;

  private EmployeeService employeeService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    employeeService = new EmployeeService(userAddressService, employmentTypeService,
        officeService, userService, stateProvinceService, countryService, userCompensationService,
        userEmergencyContactService, jobService, userStatusService, awsUtil, genderService,
        maritalStatusService, emailService, compensationFrequencyService,
        userPersonalInformationService,
        userContactInformationService, userPersonalInformationMapper, userAddressMapper,
        userContactInformationMapper, userEmergencyContactMapper, auth0Helper,
        applicationEventPublisher,
        jobUserMapper, jobUserService, userRoleService, encryptorUtil);
  }

  @Nested
  class SaveEmployeeBasicInformation {

    @BeforeEach
    void init() {
      final UserStatus userStatus = new UserStatus();
      userStatus.setName(Status.PENDING_VERIFICATION.name());
      Mockito.when(userStatusService.findByName(Mockito.anyString())).thenReturn(userStatus);

      final com.auth0.json.mgmt.users.User user = new com.auth0.json.mgmt.users.User();
      user.setId("1");
      user.setId(UUID.randomUUID().toString().replaceAll("-", ""));
      Mockito.when(auth0Helper.addUser(Mockito.anyString(), Mockito.any(), Mockito.anyString()))
          .thenReturn(user);

      Mockito.when(genderService.findById(Mockito.anyString())).thenReturn(new Gender());
      Mockito.when(maritalStatusService.findById(Mockito.anyString()))
          .thenReturn(new MaritalStatus());
    }

    @Test
    void testSaveEmployeeBasicInformation() throws Exception {
      final User currentUser = new User();
      final Company company = new Company();
      company.setId("1");
      company.setName(RandomStringUtils.randomAlphabetic(4));
      currentUser.setCompany(company);

      final EmployeeDto employeeDto = new EmployeeDto();
      employeeDto.setEmailWork("example@indeed.com");

      final String originalHexString = FileType.PNG.getValue();
      final byte[] imageBytes = Hex.decodeHex(originalHexString);
      String imageString = Base64.getEncoder().encodeToString(imageBytes);

      imageString = "x," + imageString;
      employeeDto.setPersonalPhoto(imageString);

      final UserPersonalInformationDto userPersonalInformationDto = new UserPersonalInformationDto();
      userPersonalInformationDto.setGenderId("1");
      userPersonalInformationDto.setMaritalStatusId("1");
      employeeDto.setUserPersonalInformationDto(userPersonalInformationDto);

      final UserContactInformationDto userContactInformationDto = new UserContactInformationDto();
      employeeDto.setUserContactInformationDto(userContactInformationDto);

      Whitebox.invokeMethod(employeeService, "saveEmployeeBasicInformation",
          currentUser, employeeDto);
      Mockito.verify(userService, Mockito.times(1)).save(Mockito.any());
    }
  }

  @Nested
  class ResendEmail {

    private User user;

    private EmailResendDto emailResendDto;

    @BeforeEach
    void init() {
      user = new User();
      user.setId(RandomStringUtils.randomAlphabetic(16));
      final String originalEmail = "originalemail@example.com";
      final UserContactInformation userContactInformation = new UserContactInformation();
      userContactInformation.setEmailWork(originalEmail);
      user.setUserContactInformation(userContactInformation);
      final UserStatus userStatus = new UserStatus(Status.PENDING_VERIFICATION.name());
      user.setUserStatus(userStatus);
      Mockito.when(userService.findById(Mockito.anyString())).thenReturn(user);

      emailResendDto = new EmailResendDto();
      emailResendDto.setUserId(RandomStringUtils.randomAlphabetic(16));
    }

    @Test
    void whenNotInPending_thenShouldThrow() {
      final UserStatus userStatus = new UserStatus(Status.ACTIVE.name());
      user.setUserStatus(userStatus);

      Assertions.assertThrows(ForbiddenException.class,
          () -> employeeService.resendEmail(emailResendDto));
    }

    @Test
    void whenChangedEmailExists_thenShouldThrow() {
      final String newEmail = "email@example.com";
      emailResendDto.setEmail(newEmail);
      Mockito.when(userService.findByEmailWork(Mockito.anyString())).thenReturn(new User());
      Assertions.assertThrows(ForbiddenException.class,
          () -> employeeService.resendEmail(emailResendDto));
    }

    @Test
    void whenChangedEmailNotExists_thenShouldSuccess() {
      final String newEmail = "email@example.com";
      emailResendDto.setEmail(newEmail);
      Mockito.when(userService.findByEmailWork(Mockito.anyString())).thenReturn(null);

      final Company company = new Company();
      company.setName(RandomStringUtils.randomAlphabetic(4));
      user.setCompany(company);

      Mockito.when(emailService
          .findFirstByToAndSubjectOrderBySendDateDesc(Mockito.anyString(), Mockito.anyString()))
          .thenReturn(new Email());
      employeeService.resendEmail(emailResendDto);
      Mockito.verify(userService, Mockito.times(1)).save(Mockito.any());
      Mockito.verify(emailService, Mockito.times(1))
          .saveAndScheduleEmail(Mockito.any());
    }
  }

  @Nested
  class FindPersonalMessage {

    private String targetUserId;

    private String userId;

    private User targetUser;

    private User currentUser;

    @BeforeEach
    void init() {
      targetUserId = RandomStringUtils.randomAlphabetic(16);
      userId = RandomStringUtils.randomAlphabetic(16);

      targetUser = new User();
      targetUser.setId(targetUserId);
      final UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
      targetUser.setUserPersonalInformation(userPersonalInformation);

      currentUser = new User();
      currentUser.setId(userId);
      final UserRole userRole = new UserRole();
      userRole.setName(Role.MANAGER.name());
      currentUser.setUserRole(userRole);
      final UserPersonalInformation currentUserPersonalInformation = new UserPersonalInformation();
      currentUser.setUserPersonalInformation(currentUserPersonalInformation);

      Mockito.when(userService.findById(targetUserId)).thenReturn(targetUser);
      Mockito.when(userService.findById(userId)).thenReturn(currentUser);
    }

    @Test
    void whenIsCurrentUser_thenReturnEmployeePersonalInformation() {
      final BasicUserPersonalInformationDto personalInformation = employeeService
          .findPersonalMessage(userId, userId);
      Assertions.assertTrue(personalInformation instanceof EmployeePersonalInformationDto);
    }

    @Test
    void whenIsAdmin_thenReturnEmployeePersonalInformation() {
      final UserRole admin = new UserRole();
      admin.setName(Role.ADMIN.name());
      currentUser.setUserRole(admin);
      final BasicUserPersonalInformationDto personalInformation = employeeService
          .findPersonalMessage(targetUserId, userId);
      Assertions.assertTrue(personalInformation instanceof EmployeePersonalInformationDto);
    }

    @Test
    void whenIsUserManager_thenReturnPersonalInformationDtoForManager() {
      targetUser.setManagerUser(currentUser);
      final BasicUserPersonalInformationDto personalInformation = employeeService
          .findPersonalMessage(targetUserId, userId);
      Assertions.assertTrue(personalInformation instanceof UserPersonalInformationForManagerDto);
    }

    @Test
    void whenIsEmployee_thenReturnBasicInformationDto() {
      final User managerUser = new User();
      managerUser.setId(RandomStringUtils.randomAlphabetic(16));
      targetUser.setManagerUser(managerUser);

      final BasicUserPersonalInformationDto personalInformation = employeeService
          .findPersonalMessage(targetUserId, userId);
      Assertions.assertNotNull(personalInformation);
    }

  }

  @Nested
  class FindContactMessage {


    private String targetUserId;

    private String userId;

    private User targetUser;

    private User currentUser;

    @BeforeEach
    void init() {
      targetUserId = RandomStringUtils.randomAlphabetic(16);
      userId = RandomStringUtils.randomAlphabetic(16);

      targetUser = new User();
      targetUser.setId(targetUserId);
      final UserContactInformation userContactInformation = new UserContactInformation();
      targetUser.setUserContactInformation(userContactInformation);

      currentUser = new User();
      currentUser.setId(userId);
      final UserRole userRole = new UserRole();
      userRole.setName(Role.MANAGER.name());
      currentUser.setUserRole(userRole);
      Mockito.when(userService.findById(targetUserId)).thenReturn(targetUser);
      Mockito.when(userService.findById(userId)).thenReturn(currentUser);
    }

    @Test
    void whenIsCurrentUser_thenReturnEmployeeContactInformationInstance() {
      final BasicUserContactInformationDto contactInformation = employeeService
          .findContactMessage(targetUserId, targetUserId);
      Assertions.assertTrue(contactInformation instanceof EmployeeContactInformationDto);
    }

    @Test
    void whenIsManager_thenReturnEmployeeContactInformationInstance() {
      targetUser.setManagerUser(currentUser);
      final BasicUserContactInformationDto contactInformation = employeeService
          .findContactMessage(targetUserId, userId);
      Assertions.assertTrue(contactInformation instanceof EmployeeContactInformationDto);
    }

    @Test
    void whenIsAdmin_thenReturnEmployeeContactInformationInstance() {
      final UserRole userRole = new UserRole();
      userRole.setName(Role.ADMIN.name());
      currentUser.setUserRole(userRole);

      final User managerUser = new User();
      managerUser.setId(RandomStringUtils.randomAlphabetic(16));
      targetUser.setManagerUser(managerUser);
      final BasicUserContactInformationDto contactInformation = employeeService
          .findContactMessage(targetUserId, userId);
      Assertions.assertTrue(contactInformation instanceof EmployeeContactInformationDto);
    }

    @Test
    void whenIsEmployee_thenReturnBasicContactInformation() {
      final User managerUser = new User();
      managerUser.setId(RandomStringUtils.randomAlphabetic(16));
      targetUser.setManagerUser(managerUser);
      final BasicUserContactInformationDto contactInformation = employeeService
          .findContactMessage(targetUserId, userId);
      Assertions.assertNotNull(contactInformation);
    }
  }
}
