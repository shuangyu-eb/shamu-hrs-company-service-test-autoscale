package shamu.company.payroll.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.attendance.service.AttendanceTeamHoursService;
import shamu.company.employee.dto.CompensationDto;
import shamu.company.job.entity.JobUser;
import shamu.company.job.service.JobUserService;
import shamu.company.payroll.dto.PayrollDetailDto;
import shamu.company.payroll.dto.PayrollSetupEmployeeDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.service.UserCompensationService;
import shamu.company.user.service.UserService;

@Service
public class PayrollSetUpService {

  private final AttendanceSetUpService attendanceSetUpService;
  private final AttendanceTeamHoursService attendanceTeamHoursService;
  private final UserService userService;
  private final UserCompensationService userCompensationService;
  private final UserCompensationMapper userCompensationMapper;
  private final JobUserService jobUserService;

  public PayrollSetUpService(
      final AttendanceSetUpService attendanceSetUpService,
      final AttendanceTeamHoursService attendanceTeamHoursService,
      final UserService userService,
      final UserCompensationService userCompensationService,
      final UserCompensationMapper userCompensationMapper,
      final JobUserService jobUserService) {
    this.attendanceSetUpService = attendanceSetUpService;
    this.attendanceTeamHoursService = attendanceTeamHoursService;
    this.userService = userService;
    this.userCompensationService = userCompensationService;
    this.userCompensationMapper = userCompensationMapper;
    this.jobUserService = jobUserService;
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

  public List<PayrollSetupEmployeeDto> getPayrollSetUpEmployees() {
    final List<User> users = userService.findAllUsersByCompany();
    return users.stream()
        .map(
            (user -> {
              // get user compensation
              final UserCompensation userCompensation =
                  userCompensationService.findByUserId(user.getId());
              final CompensationDto compensationDto =
                  userCompensationMapper.convertToCompensationDto(userCompensation);

              // get employee type
              final JobUser jobUser = jobUserService.findJobUserByUser(user);
              final String employeeType = jobUser.getEmployeeType().getName();
              final String startDate = jobUser.getStartDate().toString();
              return new PayrollSetupEmployeeDto(
                  user.getId(),
                  user.getImageUrl(),
                  user.getUserPersonalInformation().getName(),
                  startDate,
                  employeeType,
                  compensationDto);
            }))
        .collect(Collectors.toList());
  }
}
