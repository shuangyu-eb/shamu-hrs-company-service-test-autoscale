package shamu.company.payroll.service;

import org.springframework.stereotype.Service;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.attendance.service.AttendanceTeamHoursService;
import shamu.company.payroll.dto.PayrollDetailDto;

@Service
public class PayrollSetupService {

  private final AttendanceSetUpService attendanceSetUpService;

  private final AttendanceTeamHoursService attendanceTeamHoursService;

  public PayrollSetupService(
      final AttendanceSetUpService attendanceSetUpService,
      final AttendanceTeamHoursService attendanceTeamHoursService) {
    this.attendanceSetUpService = attendanceSetUpService;
    this.attendanceTeamHoursService = attendanceTeamHoursService;
  }

  public PayrollDetailDto getPayrollDetails() {
    final boolean isSetUp = attendanceSetUpService.findIsAttendanceSetUp();
    final PayrollDetailDto payrollDetailDto = new PayrollDetailDto();
    payrollDetailDto.setExists(isSetUp);

    if (isSetUp) {
      payrollDetailDto.setPayrollDetails(attendanceTeamHoursService.findAttendanceDetails());
    }
    return payrollDetailDto;
  }
}
