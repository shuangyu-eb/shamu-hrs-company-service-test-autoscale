package shamu.company.employee;

import java.util.Base64;
import java.util.UUID;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.springframework.context.ApplicationEventPublisher;
import shamu.company.common.repository.CountryRepository;
import shamu.company.common.repository.EmploymentTypeRepository;
import shamu.company.common.repository.OfficeRepository;
import shamu.company.common.repository.StateProvinceRepository;
import shamu.company.company.entity.Company;
import shamu.company.crypto.EncryptorUtil;
import shamu.company.email.EmailRepository;
import shamu.company.email.EmailService;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.service.EmployeeService;
import shamu.company.helpers.auth0.Auth0Helper;
import shamu.company.info.entity.mapper.UserEmergencyContactMapper;
import shamu.company.info.repository.UserEmergencyContactRepository;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.repository.JobRepository;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.job.service.JobUserService;
import shamu.company.s3.AwsUtil;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.entity.mapper.UserAddressMapper;
import shamu.company.user.entity.mapper.UserContactInformationMapper;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.repository.CompensationFrequencyRepository;
import shamu.company.user.repository.UserAddressRepository;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.user.service.GenderService;
import shamu.company.user.service.MaritalStatusService;
import shamu.company.user.service.UserContactInformationService;
import shamu.company.user.service.UserPersonalInformationService;
import shamu.company.user.service.UserRoleService;
import shamu.company.user.service.UserService;
import shamu.company.user.service.UserStatusService;
import shamu.company.utils.FileValidateUtil.FileType;

class EmployeeServiceTests {

  @Mock private JobUserRepository jobUserRepository;
  @Mock private UserAddressRepository userAddressRepository;
  @Mock private EmploymentTypeRepository employmentTypeRepository;
  @Mock private UserCompensationRepository userCompensationRepository;
  @Mock private JobRepository jobRepository;
  @Mock
  private GenderService genderService;
  @Mock
  private MaritalStatusService maritalStatusService;
  @Mock private AwsUtil awsUtil;
  @Mock private CompensationFrequencyRepository compensationFrequencyRepository;
  @Mock private StateProvinceRepository stateProvinceRepository;
  @Mock private CountryRepository countryRepository;
  @Mock private UserEmergencyContactRepository userEmergencyContactRepository;
  @Mock private UserService userService;
  @Mock
  private UserStatusService userStatusService;
  @Mock private EmailRepository emailRepository;
  @Mock private EmailService emailService;
  @Mock private OfficeRepository officeRepository;
  @Mock private UserPersonalInformationService userPersonalInformationService;
  @Mock private UserContactInformationService userContactInformationService;
  @Mock private UserPersonalInformationMapper userPersonalInformationMapper;
  @Mock private UserAddressMapper userAddressMapper;
  @Mock private UserContactInformationMapper userContactInformationMapper;
  @Mock private UserEmergencyContactMapper userEmergencyContactMapper;
  @Mock
  private Auth0Helper auth0Helper;
  @Mock private ApplicationEventPublisher applicationEventPublisher;
  @Mock private JobUserMapper jobUserMapper;
  @Mock private JobUserService jobUserService;
  @Mock private UserMapper userMapper;
  @Mock private UserRoleService userRoleService;
  @Mock
  private EncryptorUtil encryptorUtil;

  @InjectMocks
  private EmployeeService employeeService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class SaveEmployeeBasicInformation {

    @BeforeEach
    void init() {
      Mockito.when(userPersonalInformationMapper.createFromUserPersonalInformationDto(Mockito.any()))
          .thenReturn(null);

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

      Mockito.when(userContactInformationMapper.createFromUserContactInformationDto(Mockito.any()))
          .thenReturn(new UserContactInformation());
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
}
