package shamu.company.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.entity.EmployeesTaSetting;
import shamu.company.attendance.repository.EmployeesTaSettingRepository;
import shamu.company.attendance.service.EmployeesTaSettingService;

import static org.assertj.core.api.Assertions.assertThatCode;

public class EmployeesTaSettingServiceTests {
  @InjectMocks EmployeesTaSettingService employeesTaSettingService;

  @Mock private EmployeesTaSettingRepository employeesTaSettingRepository;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void isEnrolledInTA() {
    final EmployeesTaSetting employeesTaSetting = new EmployeesTaSetting();
    Mockito.when(employeesTaSettingRepository.findByEmployeeId(Mockito.any()))
        .thenReturn(employeesTaSetting);
    assertThatCode(() -> employeesTaSettingService.isEnrolledInTA("1")).doesNotThrowAnyException();
  }
}
