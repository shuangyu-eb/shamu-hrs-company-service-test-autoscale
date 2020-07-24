package shamu.company.attendance;

import static org.assertj.core.api.Assertions.assertThatCode;

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
import shamu.company.user.repository.UserRepository;

public class AttendanceSettingsServiceTests {

  @InjectMocks AttendanceSettingsService attendanceSettingsService;

  @Mock private CompanyTaSettingRepository companyTaSettingRepository;

  @Mock private EmployeesTaSettingRepository employeesTaSettingRepository;

  @Mock private StaticTimeZoneRepository staticTimeZoneRepository;

  @Mock private PayPeriodFrequencyRepository payPeriodFrequencyRepository;

  @Mock private CompanyTaSettingsMapper companyTaSettingsMapper;

  @Mock private UserRepository userRepository;

  @Mock private EmployeesTaSettingsMapper employeesTaSettingsMapper;

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
}
