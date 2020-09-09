package shamu.company.attendance;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.dto.CompanyTaSettingsDto;
import shamu.company.attendance.dto.EmployeesTaSettingDto;
import shamu.company.attendance.entity.StaticCompanyPayFrequencyType;
import shamu.company.attendance.entity.mapper.CompanyTaSettingsMapper;
import shamu.company.attendance.entity.mapper.EmployeesTaSettingsMapper;
import shamu.company.attendance.repository.CompanyTaSettingRepository;
import shamu.company.attendance.repository.EmployeesTaSettingRepository;
import shamu.company.attendance.repository.PayPeriodFrequencyRepository;
import shamu.company.attendance.repository.StaticTimeZoneRepository;
import shamu.company.attendance.service.AttendanceSettingsService;
import shamu.company.attendance.service.TimeSheetService;
import shamu.company.common.entity.mapper.PayrollDetailMapper;
import shamu.company.common.service.PayrollDetailService;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.OfficeAddress;
import shamu.company.helpers.googlemaps.GoogleMapsHelper;
import shamu.company.job.entity.JobUser;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.user.entity.User;
import shamu.company.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThatCode;

public class AttendanceSettingsServiceTests {

  @InjectMocks AttendanceSettingsService attendanceSettingsService;

  @Mock private CompanyTaSettingRepository companyTaSettingRepository;

  @Mock private EmployeesTaSettingRepository employeesTaSettingRepository;

  @Mock private StaticTimeZoneRepository staticTimeZoneRepository;

  @Mock private PayPeriodFrequencyRepository payPeriodFrequencyRepository;

  @Mock private CompanyTaSettingsMapper companyTaSettingsMapper;

  @Mock private UserRepository userRepository;

  @Mock private EmployeesTaSettingsMapper employeesTaSettingsMapper;

  @Mock private TimeSheetService timeSheetService;

  @Mock private PayrollDetailService payrollDetailService;

  @Mock private PayrollDetailMapper payrollDetailMapper;

  @Mock private JobUserRepository jobUserRepository;

  @Mock private GoogleMapsHelper googleMapsHelper;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void findEmployeesSettings() {
    assertThatCode(() -> attendanceSettingsService.findEmployeesSettings("1"))
        .doesNotThrowAnyException();
  }

  @Test
  void findAllStaticTimeZones() {
    assertThatCode(() -> attendanceSettingsService.findAllStaticTimeZones())
        .doesNotThrowAnyException();
  }

  @Test
  void updateCompanySettings() {
    final StaticCompanyPayFrequencyType staticCompanyPayFrequencyType =
        new StaticCompanyPayFrequencyType();
    final CompanyTaSettingsDto companyTaSettingsDto = new CompanyTaSettingsDto();
    companyTaSettingsDto.setPayFrequencyType(staticCompanyPayFrequencyType);
    staticCompanyPayFrequencyType.setId("frequencyType");
    Mockito.when(payPeriodFrequencyRepository.findByName(Mockito.any()))
        .thenReturn(staticCompanyPayFrequencyType);
    assertThatCode(
            () ->
                attendanceSettingsService.updateCompanySettings(companyTaSettingsDto, "companyId"))
        .doesNotThrowAnyException();
  }

  @Test
  void updateEmployeeSettings() {
    assertThatCode(
            () ->
                attendanceSettingsService.updateEmployeeSettings(
                    "employeeId", new EmployeesTaSettingDto()))
        .doesNotThrowAnyException();
  }

  @Test
  void findEmployeeIsAttendanceSetUp() {
    assertThatCode(() -> attendanceSettingsService.findEmployeeIsAttendanceSetUp("employeeId"))
        .doesNotThrowAnyException();
  }

  @Test
  void initialTimezoneForOldDatas() {
    final List<User> userList = new ArrayList<>();
    final User user = new User();
    final Company company = new Company();
    company.setId("companyId");
    user.setId("userId");
    user.setCompany(company);
    userList.add(user);
    final JobUser jobUser = new JobUser();
    final Office office = new Office();
    final OfficeAddress officeAddress = new OfficeAddress();
    officeAddress.setPostalCode("02114");
    office.setOfficeAddress(officeAddress);
    jobUser.setOffice(office);
    Mockito.when(jobUserRepository.findJobUserByUser(user)).thenReturn(jobUser);
    Mockito.when(userRepository.findAllByTimeZoneIsNull()).thenReturn(userList);
    assertThatCode(() -> attendanceSettingsService.initialTimezoneForOldDatas())
        .doesNotThrowAnyException();
  }
}
