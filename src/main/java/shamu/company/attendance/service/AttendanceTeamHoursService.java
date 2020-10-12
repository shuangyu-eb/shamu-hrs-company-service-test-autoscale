package shamu.company.attendance.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.dto.AttendanceDetailDto;
import shamu.company.attendance.dto.AttendanceSummaryDto;
import shamu.company.attendance.dto.AttendanceTeamHoursDto;
import shamu.company.attendance.dto.EmployeeAttendanceSummaryDto;
import shamu.company.attendance.dto.PendingCountDto;
import shamu.company.attendance.dto.TeamHoursPageInfoDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.entity.Timesheet;
import shamu.company.common.entity.PayrollDetail;
import shamu.company.common.service.PayrollDetailService;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserCompensationService;
import shamu.company.user.service.UserService;
import shamu.company.utils.DateUtil;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Service
public class AttendanceTeamHoursService {
  private static final int CONVERT_HOUR_TO_MIN = 60;
  private static final String TEAM_HOURS_TYPE = "team_hours";
  private static final String COMPANY_HOURS_TYPE = "company_hours";
  private static final double ONE_POINT_FIVE_RATE = 1.5;
  private static final double TWO_RATE = 2;
  private static final String DATE_FORMAT = "MM/dd/yyyy";

  private final TimeSheetService timeSheetService;
  private final AttendanceMyHoursService attendanceMyHoursService;
  private final AttendanceSettingsService attendanceSettingsService;
  private final OvertimeService overtimeService;
  private final TimeOffRequestService timeOffRequestService;
  private final StaticTimesheetStatusService staticTimesheetStatusService;
  private final TimePeriodService timePeriodService;
  private final EmployeesTaSettingService employeesTaSettingService;
  private final PayrollDetailService payrollDetailService;
  private final UserCompensationService userCompensationService;
  private final UserService userService;

  public AttendanceTeamHoursService(
      final TimeSheetService timeSheetService,
      final AttendanceMyHoursService attendanceMyHoursService,
      final AttendanceSettingsService attendanceSettingsService,
      final OvertimeService overtimeService,
      final TimeOffRequestService timeOffRequestService,
      final StaticTimesheetStatusService statusService,
      final TimePeriodService timePeriodService,
      final EmployeesTaSettingService employeesTaSettingService,
      final PayrollDetailService payrollDetailService,
      final UserCompensationService userCompensationService,
      final UserService userService) {
    this.timeSheetService = timeSheetService;
    this.attendanceMyHoursService = attendanceMyHoursService;
    this.attendanceSettingsService = attendanceSettingsService;
    this.overtimeService = overtimeService;
    this.timeOffRequestService = timeOffRequestService;
    staticTimesheetStatusService = statusService;
    this.timePeriodService = timePeriodService;
    this.employeesTaSettingService = employeesTaSettingService;
    this.payrollDetailService = payrollDetailService;
    this.userCompensationService = userCompensationService;
    this.userService = userService;
  }

  public List<EmployeeAttendanceSummaryDto> findEmployeeAttendanceSummary(
      final String timePeriodId, final String userId, final String hourType) {
    final CompanyTaSetting companyTaSetting = attendanceSettingsService.findCompanySetting();
    final List<EmployeeAttendanceSummaryDto> employeeAttendanceSummaryDtoList = new ArrayList<>();

    final List<Timesheet> timesheets =
        findTimeSheetsByIdAndCompanyIdAndStatusAndType(
            userId,
            timePeriodId,
            Arrays.asList(
                StaticTimesheetStatus.TimeSheetStatus.SUBMITTED.name(),
                StaticTimesheetStatus.TimeSheetStatus.APPROVED.name()),
            hourType);
    timesheets.forEach(
        timesheet -> {
          final User user = timesheet.getEmployee();

          final List<EmployeeTimeLog> workedMinutes =
              attendanceMyHoursService.findAllRelevantTimelogs(timesheet, companyTaSetting);
          final Map<Double, Integer> overtimeMinutes =
              overtimeService.findAllOvertimeHours(workedMinutes, timesheet, companyTaSetting);

          int totalOvertimeMin = 0;
          int totalOvertime15Min = 0;
          int totalOvertime2Min = 0;
          for (final Map.Entry<Double, Integer> overtimeMinute : overtimeMinutes.entrySet()) {
            totalOvertimeMin += overtimeMinute.getValue();
            if (overtimeMinute.getKey() == ONE_POINT_FIVE_RATE) {
              totalOvertime15Min += overtimeMinute.getValue();
            } else if (overtimeMinute.getKey() == TWO_RATE) {
              totalOvertime2Min += overtimeMinute.getValue();
            }
          }

          final int workedMin =
              attendanceMyHoursService.getTotalNumberOfWorkedMinutes(workedMinutes, timesheet);

          final TimePeriod timePeriod = timesheet.getTimePeriod();

          employeeAttendanceSummaryDtoList.add(
              EmployeeAttendanceSummaryDto.builder()
                  .firstName(user.getUserPersonalInformation().getFirstName())
                  .lastName(user.getUserPersonalInformation().getLastName())
                  .totalMinutes(workedMin)
                  .regularMinutes(workedMin - totalOvertimeMin)
                  .overtime15Minutes(totalOvertime15Min)
                  .overtime2Minutes(totalOvertime2Min)
                  .periodStartTime(timePeriod.getStartDate())
                  .periodEndTime(timePeriod.getEndDate())
                  .approved(
                      timesheet
                          .getStatus()
                          .getName()
                          .equals(StaticTimesheetStatus.TimeSheetStatus.APPROVED.name()))
                  .build());
        });
    return employeeAttendanceSummaryDtoList;
  }

  private List<Timesheet> findTimeSheetsByIdAndCompanyIdAndStatusAndType(
      final String userId,
      final String timePeriodId,
      final List<String> timeSheetStatus,
      final String hourType) {
    if (hourType.equals(TEAM_HOURS_TYPE)) {
      return timeSheetService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
          userId, timePeriodId, timeSheetStatus);
    }
    return timeSheetService.findCompanyTimeSheetsByIdAndStatus(timePeriodId, timeSheetStatus);
  }

  public TeamHoursPageInfoDto findTeamTimeSheetsByIdAndCompanyIdAndStatus(
      final String timePeriodId,
      final String hourType,
      final List<String> timeSheetStatus,
      final String userId,
      final Pageable pageable) {
    Page<Timesheet> timeSheetPage = new PageImpl<>(Collections.emptyList());
    if (hourType.equals(TEAM_HOURS_TYPE)) {
      timeSheetPage =
          timeSheetService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
              timePeriodId, timeSheetStatus, userId, pageable);
    }
    if (hourType.equals(COMPANY_HOURS_TYPE)) {
      timeSheetPage =
          timeSheetService.findCompanyTimeSheetsByIdAndStatus(
              timePeriodId, timeSheetStatus, pageable);
    }
    final CompanyTaSetting companyTaSetting = attendanceSettingsService.findCompanySetting();

    final List<AttendanceTeamHoursDto> teamHoursDtos =
        timeSheetPage.getContent().stream()
            .map(
                timesheet -> {
                  final List<EmployeeTimeLog> workedMinutes =
                      attendanceMyHoursService.findAllRelevantTimelogs(timesheet, companyTaSetting);

                  final int workedMin =
                      attendanceMyHoursService.getTotalNumberOfWorkedMinutes(
                          workedMinutes, timesheet);
                  int totalOvertimeMin = 0;
                  final Map<Double, Integer> overtimeMinutes =
                      overtimeService.findAllOvertimeHours(
                          workedMinutes, timesheet, companyTaSetting);
                  for (final Map.Entry<Double, Integer> overtimeMinute :
                      overtimeMinutes.entrySet()) {
                    totalOvertimeMin += overtimeMinute.getValue();
                  }
                  return new AttendanceTeamHoursDto(
                      timesheet.getId(),
                      timesheet.getEmployee().getId(),
                      timesheet.getEmployee().getUserPersonalInformation().getName(),
                      workedMin,
                      totalOvertimeMin,
                      timesheet.getStatus().getName());
                })
            .collect(Collectors.toList());
    return new TeamHoursPageInfoDto(teamHoursDtos, timeSheetPage.getTotalPages());
  }

  public AttendanceSummaryDto findTeamHoursSummary(
      final String timePeriodId,
      final List<String> timesheetStatus,
      final String userId,
      final String hourType) {
    List<Timesheet> timesheets = new ArrayList<>();
    if (hourType.equals(TEAM_HOURS_TYPE)) {
      timesheets =
          timeSheetService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
              userId, timePeriodId, timesheetStatus);
    }
    if (hourType.equals(COMPANY_HOURS_TYPE)) {
      timesheets =
          timeSheetService.findCompanyTimeSheetsByIdAndStatus(timePeriodId, timesheetStatus);
    }
    final CompanyTaSetting companyTaSetting = attendanceSettingsService.findCompanySetting();
    int totalTimeOffHours = 0;
    int totalOvertimeMin = 0;
    int totalRegularMin = 0;
    for (final Timesheet timesheet : timesheets) {
      final User user = timesheet.getEmployee();
      final Timestamp timeSheetStart = timesheet.getTimePeriod().getStartDate();
      final Timestamp timesheetEnd = timesheet.getTimePeriod().getEndDate();
      final int timeOffHours =
          timeOffRequestService.findTimeOffHoursBetweenWorkPeriod(
              user, timeSheetStart.getTime(), timesheetEnd.getTime());
      totalTimeOffHours += timeOffHours;

      final List<EmployeeTimeLog> workedMinutes =
          attendanceMyHoursService.findAllRelevantTimelogs(timesheet, companyTaSetting);

      final int workedMin =
          attendanceMyHoursService.getTotalNumberOfWorkedMinutes(workedMinutes, timesheet);

      final Map<Double, Integer> overtimeMinutes =
          overtimeService.findAllOvertimeHours(workedMinutes, timesheet, companyTaSetting);
      int currentOvertimeMin = 0;
      for (final Map.Entry<Double, Integer> overtimeMinute : overtimeMinutes.entrySet()) {
        currentOvertimeMin += overtimeMinute.getValue();
      }

      totalRegularMin += workedMin - currentOvertimeMin;
      totalOvertimeMin += currentOvertimeMin;
    }
    return AttendanceSummaryDto.builder()
        .workedMinutes(totalRegularMin)
        .overTimeMinutes(totalOvertimeMin)
        .totalPtoMinutes(totalTimeOffHours * CONVERT_HOUR_TO_MIN)
        .build();
  }

  public void approvePendingHours(final Set<String> selectedTimesheets, final String approverId) {
    final User approver = userService.findById(approverId);
    final Timestamp approvedTime = DateUtil.getCurrentTime();
    final StaticTimesheetStatus timesheetApproveStatus =
        staticTimesheetStatusService.findByName(TimeSheetStatus.APPROVED.name());
    final List<Timesheet> timesheets = timeSheetService.findAllById(selectedTimesheets);
    timesheets.forEach(
        timesheet -> {
          timesheet.setStatus(timesheetApproveStatus);
          timesheet.setApproverEmployee(approver);
          timesheet.setApprovedTimestamp(approvedTime);
        });
    timeSheetService.saveAll(timesheets);
  }

  public AttendanceDetailDto findAttendanceDetails() {
    final PayrollDetail payrollDetail = payrollDetailService.find();
    final TimePeriod timePeriod = timePeriodService.findCompanyCurrentPeriod();
    final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    final String payDate = sdf.format(payrollDetail.getLastPayrollPayday());
    final String periodStartDate = sdf.format(timePeriod.getStartDate());
    final String periodEndDate = sdf.format(timePeriod.getEndDate());
    return AttendanceDetailDto.builder()
        .payDate(payDate)
        .payPeriodFrequency(payrollDetail.getPayFrequencyType().getName())
        .periodStartDate(periodStartDate)
        .periodEndDate(periodEndDate)
        .build();
  }

  @Transactional
  public void removeAttendanceDetails(final List<String> userIds) {
    employeesTaSettingService.removeEmployees(userIds);
    timeSheetService.removeUserFromAttendance(userIds);
    userCompensationService.removeUsersFromAttendance(userIds);
  }

  public PendingCountDto findAttendancePendingCount(final String userId) {
    final TimePeriod timePeriod = timePeriodService.findCompanyCurrentPeriod();
    final int teamHoursPendingCount =
        timeSheetService.findTeamHoursPendingCount(userId, timePeriod.getId());
    final int companyHoursPendingCount =
        timeSheetService.findCompanyHoursPendingCount(timePeriod.getId());
    return PendingCountDto.builder()
        .teamHoursPendingCount(teamHoursPendingCount)
        .companyHoursPendingCount(companyHoursPendingCount)
        .build();
  }

  public void completePeriod(
      final String timePeriodId, final String hourType, final String managerId) {
    final StaticTimesheetStatus completeStatus =
        staticTimesheetStatusService.findByName(TimeSheetStatus.COMPLETE.name());
    final StaticTimesheetStatus approvedStatus =
        staticTimesheetStatusService.findByName(TimeSheetStatus.APPROVED.name());
    if (COMPANY_HOURS_TYPE.equals(hourType)) {
      timeSheetService.updateTimesheetStatusByPeriodId(
          approvedStatus.getId(), completeStatus.getId(), timePeriodId);
    } else {
      timeSheetService.updateTimesheetStatusByPeriodIdAndManagerId(
          approvedStatus.getId(), completeStatus.getId(), timePeriodId, managerId);
    }
  }
}
