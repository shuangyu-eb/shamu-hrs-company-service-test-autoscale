package shamu.company.attendance;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.dto.EmployeeOvertimeDetailsDto;
import shamu.company.attendance.dto.TimeAndAttendanceDetailsDto;
import shamu.company.attendance.dto.TimeAndAttendanceRelatedUserDto;
import shamu.company.attendance.entity.EmployeesTaSetting;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.attendance.repository.CompanyTaSettingRepository;
import shamu.company.attendance.repository.EmployeesTaSettingRepository;
import shamu.company.attendance.repository.StaticCompanyPayFrequencyTypeRepository;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.company.entity.Company;
import shamu.company.company.repository.CompanyRepository;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.user.entity.CompensationOvertimeStatus;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.repository.CompensationFrequencyRepository;
import shamu.company.user.repository.CompensationOvertimeStatusRepository;
import shamu.company.user.repository.UserCompensationRepository;
import shamu.company.user.repository.UserRepository;

public class AttendanceSetUpServiceTests {

  @InjectMocks AttendanceSetUpService attendanceSetUpService;

  @Mock private CompanyTaSettingRepository companyTaSettingRepository;

  @Mock private UserRepository userRepository;

  @Mock private EmployeesTaSettingRepository employeesTaSettingRepository;

  @Mock private JobUserRepository jobUserRepository;

  @Mock private JobUserMapper jobUserMapper;

  @Mock private StaticCompanyPayFrequencyTypeRepository payFrequencyTypeRepository;

  @Mock private CompanyRepository companyRepository;

  @Mock private CompensationFrequencyRepository compensationFrequencyRepository;

  @Mock private CompensationOvertimeStatusRepository compensationOvertimeStatusRepository;

  @Mock private UserCompensationRepository userCompensationRepository;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void findIsAttendanceSetUp() {
    Mockito.when(companyTaSettingRepository.existsByCompanyId("1")).thenReturn(true);
    attendanceSetUpService.findIsAttendanceSetUp("1");
    assertThatCode(() -> attendanceSetUpService.findIsAttendanceSetUp("1"))
        .doesNotThrowAnyException();
  }

  @Nested
  class getRelatedUsers {
    String companyId = "testCompanyId";
    List<User> unselectedUsers = new ArrayList();
    List<EmployeesTaSetting> selectedUsers = new ArrayList<>();
    User user = new User();
    TimeAndAttendanceRelatedUserDto relatedUserDto = new TimeAndAttendanceRelatedUserDto();
    JobUser employeeWithJobInfo = new JobUser();

    @BeforeEach
    void init() {}

    @Test
    void whenEmployeesAreEmpty_shouldSucceed() {
      Mockito.when(userRepository.findAllByCompanyId(companyId)).thenReturn(unselectedUsers);
      Mockito.when(employeesTaSettingRepository.findAll()).thenReturn(selectedUsers);
      assertThatCode(() -> attendanceSetUpService.getRelatedUsers(companyId))
          .doesNotThrowAnyException();
    }

    @Test
    void whenEmployeesAreNotEmpty_shouldSucceed() {
      final EmployeesTaSetting employeesTaSetting = new EmployeesTaSetting();
      UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
      UserContactInformation userContactInformation = new UserContactInformation();
      userContactInformation.setEmailWork("test@qq.com");
      userPersonalInformation.setFirstName("1");
      userPersonalInformation.setLastName("2");
      user.setUserPersonalInformation(userPersonalInformation);
      user.setUserContactInformation(userContactInformation);
      employeesTaSetting.setEmployee(user);
      selectedUsers.add(employeesTaSetting);
      final User anotherUser = new User();
      anotherUser.setId("another");
      unselectedUsers.add(anotherUser);
      Mockito.when(userRepository.findAllByCompanyId(companyId)).thenReturn(unselectedUsers);
      Mockito.when(employeesTaSettingRepository.findAll()).thenReturn(selectedUsers);
      Mockito.when(jobUserRepository.findJobUserByUser(user)).thenReturn(employeeWithJobInfo);
      Mockito.when(
              jobUserMapper.convertToTimeAndAttendanceRelatedUserDto(
                  user, employeeWithJobInfo, "123"))
          .thenReturn(relatedUserDto);
      assertThatCode(() -> attendanceSetUpService.getRelatedUsers(companyId))
          .doesNotThrowAnyException();
    }
  }

  @Nested
  class saveAttendanceDetails {
    TimeAndAttendanceDetailsDto timeAndAttendanceDetailsDto = new TimeAndAttendanceDetailsDto();
    String companyId = "testCompanyId";
    String userId = "testUserId";
    StaticCompanyPayFrequencyType staticCompanyPayFrequencyType =
        new StaticCompanyPayFrequencyType();
    List<EmployeeOvertimeDetailsDto> details = new ArrayList();

    @BeforeEach
    void init() {
      timeAndAttendanceDetailsDto.setPayDate(new Date());
      timeAndAttendanceDetailsDto.setOvertimeDetails(details);
    }

    @Test
    void whenDetailsIsEmpty_shouldSucceed() {
      Mockito.when(payFrequencyTypeRepository.findByName(Mockito.any()))
          .thenReturn(staticCompanyPayFrequencyType);
      Mockito.when(companyRepository.findCompanyById(companyId)).thenReturn(new Company());
      Mockito.when(companyTaSettingRepository.save(Mockito.any())).thenReturn(Mockito.any());
      assertThatCode(
              () ->
                  attendanceSetUpService.saveAttendanceDetails(
                      timeAndAttendanceDetailsDto, companyId))
          .doesNotThrowAnyException();
    }

    @Test
    void whenDetailsIsNotEmpty_shouldSucceed() {
      final EmployeeOvertimeDetailsDto detailsDto = new EmployeeOvertimeDetailsDto();
      detailsDto.setEmployeeId(userId);
      detailsDto.setRegularPay(7.1f);
      detailsDto.setHireDate(new Date());
      details.add(detailsDto);
      Mockito.when(userCompensationRepository.existsByUserId(Mockito.any())).thenReturn(true);
      Mockito.when(userCompensationRepository.findByUserId(Mockito.any()))
          .thenReturn(new UserCompensation());
      Mockito.when(compensationFrequencyRepository.findById(Mockito.any()))
          .thenReturn(Optional.of(new CompensationFrequency()));
      Mockito.when(compensationOvertimeStatusRepository.findById(Mockito.any()))
          .thenReturn(Optional.of(new CompensationOvertimeStatus()));
      Mockito.when(jobUserRepository.findByUserId(Mockito.any())).thenReturn(new JobUser());
      assertThatCode(
              () ->
                  attendanceSetUpService.saveAttendanceDetails(
                      timeAndAttendanceDetailsDto, companyId))
          .doesNotThrowAnyException();
    }
  }
}
