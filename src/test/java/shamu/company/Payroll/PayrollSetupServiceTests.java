package shamu.company.Payroll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import shamu.company.attendance.dto.AttendanceDetailDto;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.attendance.service.AttendanceTeamHoursService;
import shamu.company.payroll.service.PayrollSetupService;

import static org.assertj.core.api.Assertions.assertThatCode;

public class PayrollSetupServiceTests {
  @InjectMocks private PayrollSetupService payrollSetupService;

  @Mock private AttendanceSetUpService attendanceSetUpService;

  @Mock private AttendanceTeamHoursService attendanceTeamHoursService;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Nested
  class getPayrollDetails {
    AttendanceDetailDto attendanceDetailDto = new AttendanceDetailDto();

    @Test
    void whenIsSetUp_thenReturnWithDetails() {
      Mockito.when(attendanceSetUpService.findIsAttendanceSetUp()).thenReturn(true);
      Mockito.when(attendanceTeamHoursService.findAttendanceDetails())
          .thenReturn(attendanceDetailDto);

      assertThatCode(() -> payrollSetupService.getPayrollDetails()).doesNotThrowAnyException();
    }
  }
}
