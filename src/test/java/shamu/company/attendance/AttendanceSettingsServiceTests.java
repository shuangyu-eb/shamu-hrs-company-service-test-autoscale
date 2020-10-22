package shamu.company.attendance;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.dto.CompanyTaSettingsDto;
import shamu.company.attendance.dto.EmployeesTaSettingDto;
import shamu.company.attendance.entity.CompanyTaSetting;
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
import shamu.company.job.repository.JobUserRepository;
import shamu.company.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;

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
    assertThatCode(() -> attendanceSettingsService.updateCompanySettings(companyTaSettingsDto))
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

  @Nested
  class TestFindCompanySetting {

    @Test
    void whenSettingIsNeverSaved_thenRuturnNull() {
      Assertions.assertThat(attendanceSettingsService.findCompanySetting()).isNull();
    }

    @Test
    void whenSettingIsSaved_thenReturnSetting() {
      final List<CompanyTaSetting> companyTaSettings =
          Collections.singletonList(new CompanyTaSetting());
      Mockito.when(companyTaSettingRepository.findAll()).thenReturn(companyTaSettings);
      Assertions.assertThat(attendanceSettingsService.findCompanySetting())
          .isEqualTo(companyTaSettings.get(0));
    }
  }

  @Test
  void findOvertimeAlertMinutes() {
    final CompanyTaSetting companyTaSetting = new CompanyTaSetting();
    companyTaSetting.setOvertimeAlert(30);
    Mockito.when(companyTaSettingRepository.findAll())
        .thenReturn(Collections.singletonList(companyTaSetting));
    assertThatCode(() -> attendanceSettingsService.findOvertimeAlertMinutes())
        .doesNotThrowAnyException();
  }
}
