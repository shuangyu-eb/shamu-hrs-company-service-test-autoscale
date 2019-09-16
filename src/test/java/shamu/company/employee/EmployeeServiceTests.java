package shamu.company.employee;

import java.util.Base64;
import java.util.UUID;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import shamu.company.email.EmailRepository;
import shamu.company.email.EmailService;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.service.impl.EmployeeServiceImpl;
import shamu.company.info.entity.mapper.UserEmergencyContactMapper;
import shamu.company.info.repository.UserEmergencyContactRepository;
import shamu.company.job.repository.JobRepository;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.user.dto.UserContactInformationDto;
import shamu.company.user.dto.UserPersonalInformationDto;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.entity.mapper.UserAddressMapper;
import shamu.company.user.entity.mapper.UserContactInformationMapper;
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.repository.CompensationFrequencyRepository;
import shamu.company.user.repository.GenderRepository;
import shamu.company.user.repository.MaritalStatusRepository;
import shamu.company.user.repository.UserAddressRepository;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.repository.UserRoleRepository;
import shamu.company.user.repository.UserStatusRepository;
import shamu.company.user.service.UserContactInformationService;
import shamu.company.user.service.UserPersonalInformationService;
import shamu.company.user.service.UserService;
import shamu.company.utils.Auth0Util;
import shamu.company.utils.AwsUtil;

public class EmployeeServiceTests {

  @Mock private UserRepository userRepository;
  @Mock private JobUserRepository jobUserRepository;
  @Mock private UserAddressRepository userAddressRepository;
  @Mock private EmploymentTypeRepository employmentTypeRepository;
  @Mock private UserCompensationRepository userCompensationRepository;
  @Mock private JobRepository jobRepository;
  @Mock private GenderRepository genderRepository;
  @Mock private MaritalStatusRepository maritalStatusRepository;
  @Mock private AwsUtil awsUtil;
  @Mock private CompensationFrequencyRepository compensationFrequencyRepository;
  @Mock private StateProvinceRepository stateProvinceRepository;
  @Mock private CountryRepository countryRepository;
  @Mock private UserEmergencyContactRepository userEmergencyContactRepository;
  @Mock private UserService userService;
  @Mock private UserRoleRepository userRoleRepository;
  @Mock private UserStatusRepository userStatusRepository;
  @Mock private EmailRepository emailRepository;
  @Mock private EmailService emailService;
  @Mock private OfficeRepository officeRepository;
  @Mock private UserPersonalInformationService userPersonalInformationService;
  @Mock private UserContactInformationService userContactInformationService;
  @Mock private UserPersonalInformationMapper userPersonalInformationMapper;
  @Mock private UserAddressMapper userAddressMapper;
  @Mock private UserContactInformationMapper userContactInformationMapper;
  @Mock private UserEmergencyContactMapper userEmergencyContactMapper;
  @Mock private Auth0Util auth0Util;
  @Mock private ApplicationEventPublisher applicationEventPublisher;

  private EmployeeServiceImpl employeeService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    employeeService = new EmployeeServiceImpl(userAddressRepository, userRepository,
        jobUserRepository, employmentTypeRepository, officeRepository, userService,
        stateProvinceRepository, countryRepository, userCompensationRepository,
        userEmergencyContactRepository, jobRepository, userRoleRepository, userStatusRepository,
        awsUtil, genderRepository, maritalStatusRepository, emailService,
        compensationFrequencyRepository, emailRepository, userPersonalInformationService,
        userContactInformationService, userPersonalInformationMapper, userAddressMapper,
        userContactInformationMapper, userEmergencyContactMapper, auth0Util,
        applicationEventPublisher);
  }

  @Nested
  class SaveEmployeeBasicInformation {

    @BeforeEach
    void init() {
      Mockito.when(userRepository.findExistingUserCountByCompanyId(Mockito.anyLong()))
          .thenReturn(0);

      Mockito.when(userPersonalInformationMapper.createFromUserPersonalInformationDto(Mockito.any()))
          .thenReturn(null);

      final UserRole userRole = new UserRole();
      userRole.setName(Role.NON_MANAGER.name());
      Mockito.when(userRoleRepository.findByName(Mockito.anyString())).thenReturn(userRole);

      final UserStatus userStatus = new UserStatus();
      userStatus.setName(Status.PENDING_VERIFICATION.name());
      Mockito.when(userStatusRepository.findByName(Mockito.anyString())).thenReturn(userStatus);

      final com.auth0.json.mgmt.users.User user = new com.auth0.json.mgmt.users.User();
      user.setId("1");
      user.setId(UUID.randomUUID().toString().replaceAll("-", ""));
      Mockito.when(auth0Util.addUser(Mockito.anyString(), Mockito.any(), Mockito.anyString()))
          .thenReturn(user);

      Mockito.when(genderRepository.getOne(Mockito.anyLong())).thenReturn(new Gender());
      Mockito.when(maritalStatusRepository.getOne(Mockito.anyLong()))
          .thenReturn(new MaritalStatus());

      Mockito.when(userContactInformationMapper.createFromUserContactInformationDto(Mockito.any()))
          .thenReturn(new UserContactInformation());
    }

    @Test
    void testSaveEmployeeBasicInformation() throws Exception {
      final User currentUser = new User();
      final Company company = new Company();
      company.setId(1L);
      company.setName(RandomStringUtils.randomAlphabetic(4));
      currentUser.setCompany(company);

      final EmployeeDto employeeDto = new EmployeeDto();
      employeeDto.setEmailWork("example@indeed.com");

      String imageUrl = Base64.getEncoder()
          .encodeToString(RandomStringUtils.randomAlphabetic(11).getBytes("UTF-8"));
      imageUrl = "x," + imageUrl;
      employeeDto.setPersonalPhoto(imageUrl);

      final UserPersonalInformationDto userPersonalInformationDto = new UserPersonalInformationDto();
      userPersonalInformationDto.setGenderId(1L);
      userPersonalInformationDto.setMaritalStatusId(1L);
      employeeDto.setUserPersonalInformationDto(userPersonalInformationDto);

      final UserContactInformationDto userContactInformationDto = new UserContactInformationDto();
      employeeDto.setUserContactInformationDto(userContactInformationDto);

      Whitebox.invokeMethod(employeeService, "saveEmployeeBasicInformation",
          currentUser, employeeDto);
      Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
    }
  }
}
