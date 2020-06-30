package shamu.company.employee;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.context.ApplicationEventPublisher;
import org.thymeleaf.context.Context;
import shamu.company.common.entity.StateProvince;
import shamu.company.common.exception.errormapping.AlreadyExistsException;
import shamu.company.common.exception.errormapping.ForbiddenException;
import shamu.company.common.service.CountryService;
import shamu.company.common.service.DepartmentService;
import shamu.company.common.service.OfficeService;
import shamu.company.common.service.StateProvinceService;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.mapper.OfficeAddressMapper;
import shamu.company.company.entity.mapper.OfficeAddressMapperImpl;
import shamu.company.company.entity.mapper.OfficeMapper;
import shamu.company.company.entity.mapper.OfficeMapperImpl;
import shamu.company.company.entity.mapper.StateProvinceMapper;
import shamu.company.crypto.EncryptorUtil;
import shamu.company.email.entity.Email;
import shamu.company.email.service.EmailService;
import shamu.company.employee.dto.EmailResendDto;
import shamu.company.employee.dto.EmployeeContactInformationDto;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.dto.EmployeePersonalInformationDto;
import shamu.company.employee.dto.NewEmployeeJobInformationDto;
import shamu.company.employee.dto.UserPersonalInformationForManagerDto;
import shamu.company.employee.dto.WelcomeEmailDto;
import shamu.company.employee.event.Auth0UserCreatedEvent;
import shamu.company.employee.service.EmployeeService;
import shamu.company.employee.service.EmploymentTypeService;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.helpers.s3.AwsHelper;
import shamu.company.info.dto.UserEmergencyContactDto;
import shamu.company.info.entity.mapper.UserEmergencyContactMapper;
import shamu.company.info.service.UserEmergencyContactService;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.entity.mapper.JobUserMapperImpl;
import shamu.company.job.service.JobService;
import shamu.company.job.service.JobUserService;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.user.dto.BasicUserContactInformationDto;
import shamu.company.user.dto.BasicUserPersonalInformationDto;
import shamu.company.user.dto.UserAddressDto;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserAddress;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.entity.mapper.UserAddressMapper;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.entity.mapper.UserContactInformationMapper;
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.event.UserEmailUpdatedEvent;
import shamu.company.user.service.CompensationFrequencyService;
import shamu.company.user.service.CompensationOvertimeStatusService;
import shamu.company.user.service.EmployeeTypesService;
import shamu.company.user.service.GenderService;
import shamu.company.user.service.MaritalStatusService;
import shamu.company.user.service.UserAddressService;
import shamu.company.user.service.UserCompensationService;
import shamu.company.user.service.UserContactInformationService;
import shamu.company.user.service.UserPersonalInformationService;
import shamu.company.user.service.UserRoleService;
import shamu.company.user.service.UserService;
import shamu.company.user.service.UserStatusService;
import shamu.company.utils.FileValidateUtils.FileFormat;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class EmployeeServiceTests {

  private final UserPersonalInformationMapper userPersonalInformationMapper =
      Mappers.getMapper(UserPersonalInformationMapper.class);
  private final UserAddressMapper userAddressMapper = Mappers.getMapper(UserAddressMapper.class);
  private final UserContactInformationMapper userContactInformationMapper =
      Mappers.getMapper(UserContactInformationMapper.class);
  private final UserEmergencyContactMapper userEmergencyContactMapper =
      Mappers.getMapper(UserEmergencyContactMapper.class);
  private final StateProvinceMapper stateProvinceMapper =
      Mappers.getMapper(StateProvinceMapper.class);
  private final OfficeAddressMapper officeAddressMapper =
      new OfficeAddressMapperImpl(stateProvinceMapper);
  private final OfficeMapper officeMapper = new OfficeMapperImpl(officeAddressMapper);
  private final UserCompensationMapper userCompensationMapper =
      Mappers.getMapper(UserCompensationMapper.class);
  private final JobUserMapper jobUserMapper =
      new JobUserMapperImpl(officeMapper, userCompensationMapper);
  @Mock private JobUserService jobUserService;
  @Mock private UserAddressService userAddressService;
  @Mock private EmploymentTypeService employmentTypeService;
  @Mock private UserCompensationService userCompensationService;
  @Mock private JobService jobService;
  @Mock private GenderService genderService;
  @Mock private MaritalStatusService maritalStatusService;
  @Mock private AwsHelper awsHelper;
  @Mock private CompensationFrequencyService compensationFrequencyService;
  @Mock private StateProvinceService stateProvinceService;
  @Mock private CountryService countryService;
  @Mock private UserEmergencyContactService userEmergencyContactService;
  @Mock private UserService userService;
  @Mock private UserStatusService userStatusService;
  @Mock private EmailService emailService;
  @Mock private OfficeService officeService;
  @Mock private UserPersonalInformationService userPersonalInformationService;
  @Mock private UserContactInformationService userContactInformationService;
  @Mock private Auth0Helper auth0Helper;
  @Mock private ApplicationEventPublisher applicationEventPublisher;
  @Mock private TimeOffPolicyService timeOffPolicyService;
  @Mock private UserRoleService userRoleService;
  @Mock private EncryptorUtil encryptorUtil;
  @Mock private EmployeeTypesService employeeTypesService;
  @Mock private CompensationOvertimeStatusService compensationOvertimeStatusService;
  @Mock private DepartmentService departmentService;

  private EmployeeService employeeService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    employeeService =
        new EmployeeService(
            timeOffPolicyService,
            userAddressService,
            employmentTypeService,
            officeService,
            userService,
            stateProvinceService,
            countryService,
            userCompensationService,
            userEmergencyContactService,
            jobService,
            userStatusService,
            awsHelper,
            genderService,
            maritalStatusService,
            emailService,
            compensationFrequencyService,
            userPersonalInformationService,
            userContactInformationService,
            userPersonalInformationMapper,
            userAddressMapper,
            userContactInformationMapper,
            userEmergencyContactMapper,
            auth0Helper,
            applicationEventPublisher,
            jobUserMapper,
            jobUserService,
            userRoleService,
            encryptorUtil,
            employeeTypesService,
            compensationOvertimeStatusService,
            departmentService);
  }

  @Test
  void testFindByCompanyId() {
    assertThatCode(() -> employeeService.findByCompanyId("1")).doesNotThrowAnyException();
  }

  @Test
  void testFindSubordinatesByManagerUserId() {
    assertThatCode(() -> employeeService.findSubordinatesByManagerUserId("1", "1"))
        .doesNotThrowAnyException();
  }

  @Test
  void testRemoveAuth0User() {
    final Auth0UserCreatedEvent auth0UserCreatedEvent = Mockito.mock(Auth0UserCreatedEvent.class);
    Mockito.when(auth0UserCreatedEvent.getUser()).thenReturn(new com.auth0.json.mgmt.users.User());
    assertThatCode(() -> employeeService.removeAuth0User(auth0UserCreatedEvent))
        .doesNotThrowAnyException();
  }

  @Test
  void testRestoreUserRole() {
    final UserEmailUpdatedEvent emailUpdatedEvent = Mockito.mock(UserEmailUpdatedEvent.class);
    Mockito.when(userService.findById(Mockito.anyString())).thenReturn(new User());
    assertThatCode(() -> employeeService.restoreUserRole(emailUpdatedEvent))
        .doesNotThrowAnyException();
  }

  @Test
  void testUpdateEmployeeBasicInformation() throws Exception {

    final EmployeeDto employeeDto = new EmployeeDto();
    final User employee = new User();
    final UserContactInformation userContactInformation = new UserContactInformation();
    final UserContactInformationDto userContactInformationDto = new UserContactInformationDto();
    final UserPersonalInformation information = new UserPersonalInformation();
    final UserPersonalInformationDto userPersonalInformationDto = new UserPersonalInformationDto();

    userContactInformation.setId("1");
    userPersonalInformationDto.setSsn("ssn");
    employeeDto.setPersonalPhoto("");
    employeeDto.setUserPersonalInformationDto(userPersonalInformationDto);
    employeeDto.setUserContactInformationDto(userContactInformationDto);
    employee.setImageUrl("image");
    employee.setUserPersonalInformation(information);
    employee.setId("id");
    employee.setUserContactInformation(userContactInformation);

    Whitebox.invokeMethod(employeeService, "updateEmployeeBasicInformation", employee, employeeDto);
    Mockito.verify(userService, Mockito.times(1)).save(Mockito.any());
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

      final String originalHexString = FileFormat.PNG.getSignature();
      final byte[] imageBytes = Hex.decodeHex(originalHexString);
      String imageString = Base64.getEncoder().encodeToString(imageBytes);

      imageString = "x," + imageString;
      employeeDto.setPersonalPhoto(imageString);

      final UserPersonalInformationDto userPersonalInformationDto =
          new UserPersonalInformationDto();
      userPersonalInformationDto.setGenderId("1");
      userPersonalInformationDto.setMaritalStatusId("1");
      employeeDto.setUserPersonalInformationDto(userPersonalInformationDto);

      final UserContactInformationDto userContactInformationDto = new UserContactInformationDto();
      employeeDto.setUserContactInformationDto(userContactInformationDto);

      Whitebox.invokeMethod(
          employeeService, "saveEmployeeBasicInformation", currentUser, employeeDto);
      Mockito.verify(userService, Mockito.times(1)).createNewEmployee(Mockito.any());
    }
  }

  @Nested
  class ResendEmail {

    private User user;

    private EmailResendDto emailResendDto;

    private Email email;

    @BeforeEach
    void init() {
      user = new User();
      email = new Email();
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
      assertThatExceptionOfType(ForbiddenException.class)
          .isThrownBy(() -> employeeService.resendEmail(emailResendDto));
    }

    @Test
    void whenChangedEmailExists_thenShouldThrow() {
      final String newEmail = "email@example.com";
      emailResendDto.setEmail(newEmail);
      Mockito.when(userService.findByEmailWork(Mockito.anyString())).thenReturn(new User());
      assertThatExceptionOfType(AlreadyExistsException.class)
          .isThrownBy(() -> employeeService.resendEmail(emailResendDto));
    }

    @Test
    void whenChangedEmailNotExists_thenShouldSuccess() {
      final String newEmail = "email@example.com";
      emailResendDto.setEmail(newEmail);
      Mockito.when(userService.findByEmailWork(Mockito.anyString())).thenReturn(null);

      final Company company = new Company();
      company.setName(RandomStringUtils.randomAlphabetic(4));
      user.setCompany(company);

      Mockito.when(
              emailService.findFirstByToAndSubjectOrderBySendDateDesc(
                  Mockito.anyString(), Mockito.anyString()))
          .thenReturn(new Email());
      employeeService.resendEmail(emailResendDto);
      Mockito.verify(userService, Mockito.times(1)).save(Mockito.any());
      Mockito.verify(emailService, Mockito.times(1)).saveAndScheduleEmail(Mockito.any());
    }

    @Test
    void whenEmailIsNotEmptyAndIsNotEqualOriginalEmail_thenShouldSuccess() {
      final String newEmail = "email@example.com";
      emailResendDto.setEmail(newEmail);
      email.setContent("welcome");
      Mockito.when(userService.findByEmailWork(Mockito.anyString())).thenReturn(null);

      final Company company = new Company();
      company.setName(RandomStringUtils.randomAlphabetic(4));
      user.setCompany(company);

      Mockito.when(
              emailService.findFirstByToAndSubjectOrderBySendDateDesc(
                  Mockito.anyString(), Mockito.anyString()))
          .thenReturn(email);
      Mockito.when(emailService.getEncodedEmailAddress(Mockito.any()))
          .thenReturn("email@example.com");
      employeeService.resendEmail(emailResendDto);
      Mockito.verify(emailService, Mockito.times(2)).getEncodedEmailAddress(Mockito.any());
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
      final BasicUserPersonalInformationDto personalInformation =
          employeeService.findPersonalMessage(userId, userId);
      assertThat(personalInformation).isExactlyInstanceOf(EmployeePersonalInformationDto.class);
    }

    @Test
    void whenIsAdmin_thenReturnEmployeePersonalInformation() {
      final UserRole admin = new UserRole();
      admin.setName(Role.ADMIN.name());
      currentUser.setUserRole(admin);
      final BasicUserPersonalInformationDto personalInformation =
          employeeService.findPersonalMessage(targetUserId, userId);
      assertThat(personalInformation).isExactlyInstanceOf(EmployeePersonalInformationDto.class);
    }

    @Test
    void whenIsUserManager_thenReturnPersonalInformationDtoForManager() {
      targetUser.setManagerUser(currentUser);
      final BasicUserPersonalInformationDto personalInformation =
          employeeService.findPersonalMessage(targetUserId, userId);
      assertThat(personalInformation)
          .isExactlyInstanceOf(UserPersonalInformationForManagerDto.class);
    }

    @Test
    void whenIsEmployee_thenReturnBasicInformationDto() {
      final User managerUser = new User();
      managerUser.setId(RandomStringUtils.randomAlphabetic(16));
      targetUser.setManagerUser(managerUser);

      final BasicUserPersonalInformationDto personalInformation =
          employeeService.findPersonalMessage(targetUserId, userId);
      assertThat(personalInformation).isNotNull();
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
      final BasicUserContactInformationDto contactInformation =
          employeeService.findContactMessage(targetUserId, targetUserId);
      assertThat(contactInformation).isExactlyInstanceOf(EmployeeContactInformationDto.class);
    }

    @Test
    void whenIsManager_thenReturnEmployeeContactInformationInstance() {
      targetUser.setManagerUser(currentUser);
      final BasicUserContactInformationDto contactInformation =
          employeeService.findContactMessage(targetUserId, userId);
      assertThat(contactInformation).isExactlyInstanceOf(EmployeeContactInformationDto.class);
    }

    @Test
    void whenIsAdmin_thenReturnEmployeeContactInformationInstance() {
      final UserRole userRole = new UserRole();
      userRole.setName(Role.ADMIN.name());
      currentUser.setUserRole(userRole);

      final User managerUser = new User();
      managerUser.setId(RandomStringUtils.randomAlphabetic(16));
      targetUser.setManagerUser(managerUser);
      final BasicUserContactInformationDto contactInformation =
          employeeService.findContactMessage(targetUserId, userId);
      assertThat(contactInformation).isExactlyInstanceOf(EmployeeContactInformationDto.class);
    }

    @Test
    void whenIsEmployee_thenReturnBasicContactInformation() {
      final User managerUser = new User();
      managerUser.setId(RandomStringUtils.randomAlphabetic(16));
      targetUser.setManagerUser(managerUser);
      final BasicUserContactInformationDto contactInformation =
          employeeService.findContactMessage(targetUserId, userId);
      assertThat(contactInformation).isNotNull();
    }
  }

  @Nested
  class addEmployee {
    List<UserEmergencyContactDto> userEmergencyContactDto;
    NewEmployeeJobInformationDto jobInformation;
    WelcomeEmailDto welcomeEmail;
    User currentUser;
    EmployeeDto employeeDto;
    Company company;
    Context emailContext;

    @BeforeEach
    void init() {
      userEmergencyContactDto = new ArrayList<>();
      jobInformation = new NewEmployeeJobInformationDto();
      welcomeEmail = new WelcomeEmailDto();
      emailContext = new Context();
      welcomeEmail.setPersonalInformation("a");
      welcomeEmail.setSendTo("a");
      currentUser = new User();
      employeeDto = new EmployeeDto();
      employeeDto.setUserEmergencyContactDto(userEmergencyContactDto);
      company = new Company();
      company.setName("a");
      employeeDto.setUserAddress(new UserAddressDto());
      employeeDto.setWelcomeEmail(welcomeEmail);
      currentUser.setCompany(company);
      currentUser.setResetPasswordToken("a");
      currentUser.setInvitationEmailToken("b");
      currentUser.setInvitedAt(Timestamp.from(Instant.now()));
      Mockito.when(userService.save(Mockito.any())).thenReturn(currentUser);
      Mockito.when(userService.createNewEmployee(Mockito.any())).thenReturn(currentUser);
      Mockito.when(
              emailService.getWelcomeEmailContextToEmail(
                  Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
          .thenReturn(emailContext);
    }

    @Test
    void whenJobInformationIsNull_thenShouldSuccess() {
      employeeService.addEmployee(employeeDto, currentUser);
      Mockito.verify(jobUserService, Mockito.times(0)).save(Mockito.any());
    }
  }

  @Nested
  class saveInvitedEmployeeAdditionalInformation {
    User employee;
    EmployeeDto employeeDto;
    UserPersonalInformationDto userPersonalInformationDto;
    UserContactInformationDto userContactInformationDto;
    UserContactInformation userContactInformation;

    @BeforeEach
    void init() {
      employee = new User();
      employeeDto = new EmployeeDto();
      userPersonalInformationDto = new UserPersonalInformationDto();
      userPersonalInformationDto.setSsn("a");
      userContactInformationDto = new UserContactInformationDto();
      employeeDto.setEmailWork("a");
      userContactInformation = new UserContactInformation();
      userContactInformation.setEmailWork(employeeDto.getEmailWork());
    }

    @Test
    void whenUserPersonalInformationIsNotNullAndGenderAndGenderIdIsNotNull_thenShouldSuccess()
        throws Exception {
      userPersonalInformationDto.setGenderId("a");
      employeeDto.setUserPersonalInformationDto(userPersonalInformationDto);
      Whitebox.invokeMethod(
          employeeService, "saveInvitedEmployeeAdditionalInformation", employee, employeeDto);
      Mockito.verify(genderService, Mockito.times(1)).findById(Mockito.anyString());
    }

    @Test
    void whenUserPersonalInformationIsNotNullAndGenderAndGenderIdIsNull_thenShouldSuccess()
        throws Exception {
      employeeDto.setUserPersonalInformationDto(userPersonalInformationDto);
      Whitebox.invokeMethod(
          employeeService, "saveInvitedEmployeeAdditionalInformation", employee, employeeDto);
      Mockito.verify(genderService, Mockito.times(0)).findById(Mockito.anyString());
      Mockito.verify(encryptorUtil, Mockito.times(1))
          .encryptSsn((User) Mockito.any(), Mockito.anyString(), Mockito.any());
    }

    @Test
    void
        whenUserPersonalInformationIsNotNullAndMartialStatusAndMartialStatusIdIsNotNull_thenShouldSuccess()
            throws Exception {
      userPersonalInformationDto.setMaritalStatusId("a");
      employeeDto.setUserPersonalInformationDto(userPersonalInformationDto);
      Whitebox.invokeMethod(
          employeeService, "saveInvitedEmployeeAdditionalInformation", employee, employeeDto);
      Mockito.verify(maritalStatusService, Mockito.times(1)).findById(Mockito.anyString());
    }

    @Test
    void
        whenUserPersonalInformationIsNotNullAndMartialStatusAndMartialStatusIdIsNull_thenShouldSuccess()
            throws Exception {
      employeeDto.setUserPersonalInformationDto(userPersonalInformationDto);
      Whitebox.invokeMethod(
          employeeService, "saveInvitedEmployeeAdditionalInformation", employee, employeeDto);
      Mockito.verify(maritalStatusService, Mockito.times(0)).findById(Mockito.anyString());
      Mockito.verify(encryptorUtil, Mockito.times(1))
          .encryptSsn((User) Mockito.any(), Mockito.anyString(), Mockito.any());
    }
  }

  @Nested
  class saveEmergencyContacts {
    User employee;
    UserEmergencyContactDto emergencyContactDto;
    List<UserEmergencyContactDto> emergencyContactDtos;

    @BeforeEach
    void init() {
      employee = new User();
      emergencyContactDto = new UserEmergencyContactDto();
      emergencyContactDtos = new ArrayList<>();
    }

    @Test
    void whenEmergencyContactDtosIsEmpty_thenShouldSuccess() throws Exception {
      Whitebox.invokeMethod(
          employeeService, "saveEmergencyContacts", employee, emergencyContactDtos);
      Mockito.verify(userEmergencyContactService, Mockito.times(0)).save(Mockito.any());
    }

    @Test
    void whenEmergencyContactDtosIsNotEmpty_thenShouldSuccess() throws Exception {
      emergencyContactDtos.add(emergencyContactDto);
      Whitebox.invokeMethod(
          employeeService, "saveEmergencyContacts", employee, emergencyContactDtos);
      Mockito.verify(userEmergencyContactService, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void whenEmergencyContactDtosIsNotEmptyAndStateIsNull_thenShouldSuccess() throws Exception {
      emergencyContactDtos.add(emergencyContactDto);
      Whitebox.invokeMethod(
          employeeService, "saveEmergencyContacts", employee, emergencyContactDtos);
      Mockito.verify(stateProvinceService, Mockito.times(0)).findById(Mockito.anyString());
      Mockito.verify(userEmergencyContactService, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void whenEmergencyContactDtosIsNotEmptyAndStateIsNotNull_thenShouldSuccess() throws Exception {
      emergencyContactDto.setStateId("a");
      emergencyContactDtos.add(emergencyContactDto);
      Whitebox.invokeMethod(
          employeeService, "saveEmergencyContacts", employee, emergencyContactDtos);
      Mockito.verify(stateProvinceService, Mockito.times(1)).findById(Mockito.anyString());
    }
  }

  @Nested
  class updateEmergencyContacts {
    User employee;
    UserEmergencyContactDto userEmergencyContactDto;
    List<UserEmergencyContactDto> emergencyContactDtos;
    List<String> contactIds;

    @BeforeEach
    void init() {
      employee = new User();
      employee.setId("a");
      userEmergencyContactDto = new UserEmergencyContactDto();
      emergencyContactDtos = new ArrayList<>();
      contactIds = new ArrayList<>();
    }

    @Test
    void whenUserEmergencyContactDtoIsEmpty_thenShouldSuccess() throws Exception {
      Whitebox.invokeMethod(
          employeeService, "updateEmergencyContacts", employee, emergencyContactDtos);
      Mockito.verify(userEmergencyContactService, Mockito.times(0))
          .findAllIdByUserId(Mockito.anyString());
    }

    @Test
    void
        whenUserEmergencyContactDtoIsNotEmptyAndUserEmergencyContactIdsIsNotEmpty_thenShouldSuccess()
            throws Exception {
      contactIds.add("a");
      emergencyContactDtos.add(userEmergencyContactDto);
      Mockito.when(userEmergencyContactService.findAllIdByUserId(Mockito.anyString()))
          .thenReturn(contactIds);
      Whitebox.invokeMethod(
          employeeService, "updateEmergencyContacts", employee, emergencyContactDtos);
      Mockito.verify(userEmergencyContactService, Mockito.times(1))
          .deleteInBatch(Mockito.anyList());
    }

    @Test
    void whenUserEmergencyContactDtoIsEmptyAndUserEmergencyContactIdsNotEmpty_thenShouldSuccess()
        throws Exception {
      emergencyContactDtos.add(userEmergencyContactDto);
      Mockito.when(userEmergencyContactService.findAllIdByUserId(Mockito.anyString()))
          .thenReturn(contactIds);
      Whitebox.invokeMethod(
          employeeService, "updateEmergencyContacts", employee, emergencyContactDtos);
      Mockito.verify(userEmergencyContactService, Mockito.times(0))
          .deleteInBatch(Mockito.anyList());
    }
  }

  @Nested
  class saveManagerUser {
    User user;
    NewEmployeeJobInformationDto jobInformation;
    User managerUser;
    UserRole userRole;

    @BeforeEach
    void init() {
      user = new User();
      userRole = new UserRole();
      jobInformation = new NewEmployeeJobInformationDto();
      managerUser = new User();
    }

    @Test
    void whenManagerUserIdIsEmpty_thenShouldSuccess() throws Exception {
      user.setId("a");
      Whitebox.invokeMethod(employeeService, "saveManagerUser", user, jobInformation);
      Mockito.verify(userService, Mockito.times(0)).findById(Mockito.anyString());
    }

    @Test
    void
        whenManagerUserIdIsNotEmptyAndManagerUserIdNotEqualUserIdAndRoleIsEmployee_thenShouldSuccess()
            throws Exception {
      jobInformation.setReportsTo("b");
      user.setId("a");
      userRole.setName("EMPLOYEE");
      managerUser.setUserRole(userRole);
      managerUser.setId("b");
      Mockito.when(userService.findById(Mockito.anyString())).thenReturn(managerUser);
      Whitebox.invokeMethod(employeeService, "saveManagerUser", user, jobInformation);
      Mockito.verify(userService, Mockito.times(1)).save(Mockito.any());
      Mockito.verify(userRoleService, Mockito.times(1)).getManager();
    }

    @Test
    void
        whenManagerUserIdIsNotEmptyAndManagerUserIdNotEqualUserIdAndRoleIsNotEmployee_thenShouldSuccess()
            throws Exception {
      jobInformation.setReportsTo("b");
      user.setId("a");
      managerUser.setUserRole(userRole);
      managerUser.setId("b");
      Mockito.when(userService.findById(Mockito.anyString())).thenReturn(managerUser);
      Whitebox.invokeMethod(employeeService, "saveManagerUser", user, jobInformation);
      Mockito.verify(userService, Mockito.times(1)).save(Mockito.any());
      Mockito.verify(userRoleService, Mockito.times(0)).getManager();
    }
  }

  @Nested
  class saveEmployeeJob {
    User employee;
    User currentUser;
    NewEmployeeJobInformationDto jobInformation;
    UserCompensation userCompensation;

    @BeforeEach
    void init() {
      employee = new User();
      currentUser = new User();
      jobInformation = new NewEmployeeJobInformationDto();
      userCompensation = new UserCompensation();
    }

    @Test
    void whenCompensationAndFrequencyIdIsNotNull_thenShouldSuccess() throws Exception {
      jobInformation.setEmploymentTypeId("a");
      jobInformation.setCompensation(BigInteger.valueOf(1));
      jobInformation.setCompensationFrequencyId("a");
      Mockito.when(userCompensationService.save(Mockito.any())).thenReturn(userCompensation);
      Whitebox.invokeMethod(
          employeeService, "saveEmployeeJob", employee, currentUser, jobInformation);
      Mockito.verify(compensationFrequencyService, Mockito.times(1)).findById(Mockito.anyString());
      Mockito.verify(userCompensationService, Mockito.times(1)).save(Mockito.any());
      Mockito.verify(jobUserService, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void whenEmploymentTypeIdIsNotEmpty_thenShouldSuccess() throws Exception {
      jobInformation.setEmploymentTypeId("a");
      Mockito.when(userCompensationService.save(Mockito.any())).thenReturn(userCompensation);
      Whitebox.invokeMethod(
          employeeService, "saveEmployeeJob", employee, currentUser, jobInformation);
      Mockito.verify(employmentTypeService, Mockito.times(1)).findById(Mockito.anyString());
      Mockito.verify(jobUserService, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void whenOfficeIdIsNotNull_thenShouldSuccess() throws Exception {
      jobInformation.setOfficeId("a");
      Mockito.when(userCompensationService.save(Mockito.any())).thenReturn(userCompensation);
      Whitebox.invokeMethod(
          employeeService, "saveEmployeeJob", employee, currentUser, jobInformation);
      Mockito.verify(officeService, Mockito.times(1)).findById(Mockito.anyString());
      Mockito.verify(jobUserService, Mockito.times(1)).save(Mockito.any());
    }
  }

  @Nested
  class saveEmployeeAddress {
    User employee;
    EmployeeDto employeeDto;
    StateProvince stateProvince;
    UserAddressDto userAddressDto;

    @BeforeEach
    void init() {
      employeeDto = new EmployeeDto();
      employee = new User();
      stateProvince = new StateProvince();
      userAddressDto = new UserAddressDto();
    }

    @Test
    void whenStateProvinceIsNotNull_thenShouldSuccess() throws Exception {
      userAddressDto.setStateId("a");
      employeeDto.setUserAddress(userAddressDto);
      Whitebox.invokeMethod(employeeService, "saveEmployeeAddress", employee, employeeDto);
      Mockito.verify(stateProvinceService, Mockito.times(1)).findById(Mockito.anyString());
      Mockito.verify(userAddressService, Mockito.times(1)).save((UserAddress) Mockito.any());
    }

    @Test
    void whenCountryIsNotNull_thenShouldSuccess() throws Exception {
      userAddressDto.setCountryId("a");
      employeeDto.setUserAddress(userAddressDto);
      Whitebox.invokeMethod(employeeService, "saveEmployeeAddress", employee, employeeDto);
      Mockito.verify(countryService, Mockito.times(1)).findById(Mockito.anyString());
      Mockito.verify(userAddressService, Mockito.times(1)).save((UserAddress) Mockito.any());
    }
  }

  @Nested
  class updateEmployeeAddress {
    User employee;
    EmployeeDto employeeDto;
    UserAddressDto userAddressDto;

    @BeforeEach
    void init() {
      employee = new User();
      employeeDto = new EmployeeDto();
      userAddressDto = new UserAddressDto();
    }

    @Test
    void whenStateProvinceIdIsNotEmpty_thenShouldSuccess() throws Exception {
      userAddressDto.setStateId("a");
      employeeDto.setUserAddress(userAddressDto);
      Whitebox.invokeMethod(employeeService, "updateEmployeeAddress", employee, employeeDto);
      Mockito.verify(stateProvinceService, Mockito.times(1)).findById(Mockito.anyString());
      Mockito.verify(userAddressService, Mockito.times(1)).save((UserAddress) Mockito.any());
    }

    @Test
    void whenStateCountryIdIsNotEmpty_thenShouldSuccess() throws Exception {
      userAddressDto.setCountryId("a");
      employeeDto.setUserAddress(userAddressDto);
      Whitebox.invokeMethod(employeeService, "updateEmployeeAddress", employee, employeeDto);
      Mockito.verify(stateProvinceService, Mockito.times(0)).findById(Mockito.anyString());
      Mockito.verify(userAddressService, Mockito.times(1)).save((UserAddress) Mockito.any());
    }
  }

  @Nested
  class getEmployeeInfoByUserId {
    String userId = "a";
    User employee;
    UserContactInformation userContactInformation;
    UserStatus userStatus;
    UserRole userRole;
    User managerUser;

    @BeforeEach
    void init() {
      employee = new User();
      userContactInformation = new UserContactInformation();
      userContactInformation.setEmailWork("a");
      userStatus = new UserStatus();
      userStatus.setName("ACTIVE");
      userRole = new UserRole();
      managerUser = new User();
      managerUser.setId("a");
      employee.setUserStatus(userStatus);
      employee.setUserRole(userRole);
      employee.setUserContactInformation(userContactInformation);
    }

    @Test
    void whenManagerUserIsNotNull_thenShouldSuccess() {
      employee.setManagerUser(managerUser);
      Mockito.when(userService.findById(userId)).thenReturn(employee);
      employeeService.getEmployeeInfoByUserId(userId);
      Mockito.verify(userService, Mockito.times(2))
          .findEmployeeInfoByEmployeeId(Mockito.anyString());
    }

    @Test
    void whenManagerUserIsNull_thenShouldSuccess() {
      Mockito.when(userService.findById(userId)).thenReturn(employee);
      employeeService.getEmployeeInfoByUserId(userId);
      Mockito.verify(userService, Mockito.times(1))
          .findEmployeeInfoByEmployeeId(Mockito.anyString());
    }
  }
}
