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
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType.PayFrequencyType;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.repository.EmployeesTaSettingRepository;
import shamu.company.attendance.repository.StaticTimesheetStatusRepository;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.attendance.service.AttendanceSettingsService;
import shamu.company.attendance.service.PayPeriodFrequencyService;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.company.entity.Company;
import shamu.company.company.repository.CompanyRepository;
import shamu.company.company.service.CompanyService;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.scheduler.QuartzJobScheduler;
import shamu.company.timeoff.service.PaidHolidayService;
import shamu.company.user.entity.CompensationOvertimeStatus;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.repository.CompensationFrequencyRepository;
import shamu.company.user.repository.CompensationOvertimeStatusRepository;
import shamu.company.user.repository.UserRepository;
import shamu.company.user.service.UserCompensationService;
import shamu.company.user.service.UserService;

public class AttendanceSetUpServiceTests {

  @InjectMocks AttendanceSetUpService attendanceSetUpService;

  @Mock private AttendanceSettingsService attendanceSettingsService;

  @Mock private UserRepository userRepository;

  @Mock private EmployeesTaSettingRepository employeesTaSettingRepository;

  @Mock private JobUserRepository jobUserRepository;

  @Mock private JobUserMapper jobUserMapper;

  @Mock private CompanyRepository companyRepository;

  @Mock private CompensationFrequencyRepository compensationFrequencyRepository;

  @Mock private CompensationOvertimeStatusRepository compensationOvertimeStatusRepository;

  @Mock private UserCompensationService userCompensationService;

  @Mock private UserService userService;

  @Mock private TimePeriodService timePeriodService;

  @Mock private TimeSheetService timeSheetService;

  @Mock private QuartzJobScheduler quartzJobScheduler;

  @Mock private PaidHolidayService paidHolidayService;

  @Mock private StaticTimesheetStatusRepository staticTimesheetStatusRepository;

  @Mock private UserCompensationMapper userCompensationMapper;

  @Mock private PayPeriodFrequencyService payPeriodFrequencyService;

  @Mock private CompanyService companyService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void findIsAttendanceSetUp() {
    Mockito.when(attendanceSettingsService.existsByCompanyId("1")).thenReturn(true);
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
      final UserPersonalInformation userPersonalInformation = new UserPersonalInformation();
      final UserContactInformation userContactInformation = new UserContactInformation();
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
              userService.getUserNameInUsers(employeesTaSetting.getEmployee(), unselectedUsers))
          .thenReturn("123");
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
    Company company;

    @BeforeEach
    void init() {
      company = new Company();
      timeAndAttendanceDetailsDto.setPayDate(new Date());
      timeAndAttendanceDetailsDto.setOvertimeDetails(details);
      timeAndAttendanceDetailsDto.setPeriodStartDate(new Date());
      timeAndAttendanceDetailsDto.setPeriodEndDate(new Date());
      Mockito.when(companyService.findById(Mockito.anyString())).thenReturn(company);
    }

    @Test
    void whenDetailsIsEmpty_shouldSucceed() {
      Mockito.when(payPeriodFrequencyService.findByName(Mockito.any()))
          .thenReturn(staticCompanyPayFrequencyType);
      Mockito.when(companyRepository.findCompanyById(companyId)).thenReturn(new Company());
      Mockito.when(attendanceSettingsService.saveCompanyTaSetting(Mockito.any()))
          .thenReturn(Mockito.any());
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
      detailsDto.setRegularPay(7.1d);
      detailsDto.setHireDate(new Date());
      details.add(detailsDto);
      Mockito.when(userCompensationService.existsByUserId(Mockito.any())).thenReturn(true);
      Mockito.when(userCompensationService.findByUserId(Mockito.any()))
          .thenReturn(new UserCompensation());
      Mockito.when(compensationFrequencyRepository.findById(Mockito.any()))
          .thenReturn(Optional.of(new CompensationFrequency()));
      Mockito.when(compensationOvertimeStatusRepository.findById(Mockito.any()))
          .thenReturn(Optional.of(new CompensationOvertimeStatus()));
      Mockito.when(jobUserRepository.findByUserId(Mockito.any())).thenReturn(new JobUser());
      Mockito.when(timeSheetService.saveAll(Mockito.any())).thenReturn(Mockito.any());
      assertThatCode(
              () ->
                  attendanceSetUpService.saveAttendanceDetails(
                      timeAndAttendanceDetailsDto, companyId))
          .doesNotThrowAnyException();
    }
  }

  @Nested
  class getNextPeriod {
    TimePeriod timePeriod;
    String payPeriodFrequency;
    String userId;
    Company company;

    @BeforeEach
    void init() {
      company = new Company();
      payPeriodFrequency = "WEEKLY";
      timePeriod = new TimePeriod(new Date(), new Date(), company);
    }

    @Test
    void frequencyIsValid_shouldSucceed() {
      for (final PayFrequencyType payPeriodFrequency : PayFrequencyType.values()) {
        assertThatCode(
                () ->
                    attendanceSetUpService.getNextPeriod(
                        timePeriod, payPeriodFrequency.name(), company))
            .doesNotThrowAnyException();
      }
    }

    @Test
    void userIdIsValid_shouldSucceed() {
      userId = "test_user_id";
      final User user = new User();
      user.setId(userId);
      user.setCompany(new Company());
      final StaticCompanyPayFrequencyType staticCompanyPayFrequencyType =
          new StaticCompanyPayFrequencyType();
      staticCompanyPayFrequencyType.setName(payPeriodFrequency);
      Mockito.when(timePeriodService.findUserLatestPeriod(userId))
          .thenReturn(Optional.ofNullable(timePeriod));
      Mockito.when(userService.findById(userId)).thenReturn(user);
      Mockito.when(payPeriodFrequencyService.findByCompany(Mockito.any()))
          .thenReturn(Optional.ofNullable(staticCompanyPayFrequencyType));
      assertThatCode(() -> attendanceSetUpService.findNextPeriodByUser(userId))
          .doesNotThrowAnyException();
    }
  }
}
