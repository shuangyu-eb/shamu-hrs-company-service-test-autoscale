package shamu.company.payroll.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.attendance.service.AttendanceTeamHoursService;
import shamu.company.employee.dto.CompensationDto;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.financialengine.dto.CompanyTaxIdDto;
import shamu.company.financialengine.service.FECompanyService;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.job.service.JobUserService;
import shamu.company.payroll.dto.PayrollAuthorizedEmployeeDto;
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
  private final FECompanyService feCompanyService;

  public PayrollSetUpService(
      final AttendanceSetUpService attendanceSetUpService,
      final AttendanceTeamHoursService attendanceTeamHoursService,
      final UserService userService,
      final UserCompensationService userCompensationService,
      final UserCompensationMapper userCompensationMapper,
      final JobUserService jobUserService,
      final FECompanyService feCompanyService) {
    this.attendanceSetUpService = attendanceSetUpService;
    this.attendanceTeamHoursService = attendanceTeamHoursService;
    this.userService = userService;
    this.userCompensationService = userCompensationService;
    this.userCompensationMapper = userCompensationMapper;
    this.jobUserService = jobUserService;
    this.feCompanyService = feCompanyService;
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

  public List<CompanyTaxIdDto> getTaxList() {
    return feCompanyService.getAvailableTaxList();
  }

  public Page<PayrollAuthorizedEmployeeDto> getPayrollAuthorizedEmployees(
      final EmployeeListSearchCondition employeeListSearchCondition) {
    final Page<JobUserListItem> jobUserListItems =
        userService.findAllEmployees(employeeListSearchCondition);
    final List<PayrollAuthorizedEmployeeDto> content = new ArrayList<>();
    jobUserListItems.forEach(
        jobUserListItem -> content.add(assembleAuthorizedEmployeeDto(jobUserListItem)));
    return new PageImpl<>(
        content, jobUserListItems.getPageable(), jobUserListItems.getTotalElements());
  }

  private PayrollAuthorizedEmployeeDto assembleAuthorizedEmployeeDto(
      final JobUserListItem jobUserListItem) {
    final PayrollAuthorizedEmployeeDto payrollAuthorizedEmployeeDto =
        new PayrollAuthorizedEmployeeDto();

    BeanUtils.copyProperties(jobUserListItem, payrollAuthorizedEmployeeDto);
    final String managerId = userService.getManagerUserIdById(jobUserListItem.getId());
    if (managerId != null) {
      final User manager = userService.findById(managerId);
      payrollAuthorizedEmployeeDto.setReportsToName(manager.getUserPersonalInformation().getName());
    }

    return payrollAuthorizedEmployeeDto;
  }
}
