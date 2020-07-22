package shamu.company.attendance;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.repository.CompanyTaSettingRepository;
import shamu.company.attendance.repository.EmployeesTaSettingRepository;
import shamu.company.attendance.repository.StaticTimeZoneRepository;
import shamu.company.attendance.service.AttendanceSettingsService;

public class AttendanceSettingsServiceTests {

  @InjectMocks
  AttendanceSettingsService attendanceSettingsService;

  @Mock private CompanyTaSettingRepository companyTaSettingRepository;

  @Mock private EmployeesTaSettingRepository employeesTaSettingRepository;

  @Mock private StaticTimeZoneRepository staticTimeZoneRepository;

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

}
