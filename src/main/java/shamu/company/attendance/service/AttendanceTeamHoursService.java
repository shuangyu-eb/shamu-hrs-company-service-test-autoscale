package shamu.company.attendance.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.common.entity.PayrollDetail;
import shamu.company.common.service.PayrollDetailService;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;

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
  private static final String HOUR_TYPE = "Per Hour";
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

  public AttendanceTeamHoursService(
      final TimeSheetService timeSheetService,
      final AttendanceMyHoursService attendanceMyHoursService,
      final AttendanceSettingsService attendanceSettingsService,
      final OvertimeService overtimeService,
      final TimeOffRequestService timeOffRequestService,
      final StaticTimesheetStatusService statusService,
      final TimePeriodService timePeriodService,
      final EmployeesTaSettingService employeesTaSettingService,
      final PayrollDetailService payrollDetailService) {
    this.timeSheetService = timeSheetService;
    this.attendanceMyHoursService = attendanceMyHoursService;
    this.attendanceSettingsService = attendanceSettingsService;
    this.overtimeService = overtimeService;
    this.timeOffRequestService = timeOffRequestService;
    staticTimesheetStatusService = statusService;
    this.timePeriodService = timePeriodService;
    this.employeesTaSettingService = employeesTaSettingService;
    this.payrollDetailService = payrollDetailService;
  }

  public List<EmployeeAttendanceSummaryDto> findEmployeeAttendanceSummary(
      final String timePeriodId, final String userId, final String hourType) {
    final CompanyTaSetting companyTaSetting = attendanceSettingsService.findCompanySetting();
    final List<EmployeeAttendanceSummaryDto> employeeAttendanceSummaryDtoList = new ArrayList<>();

    final List<TimeSheet> timeSheets =
        findTimeSheetsByIdAndCompanyIdAndStatusAndType(
            userId,
            timePeriodId,
            Arrays.asList(
                StaticTimesheetStatus.TimeSheetStatus.SUBMITTED.name(),
                StaticTimesheetStatus.TimeSheetStatus.APPROVED.name()),
            hourType);
    timeSheets.forEach(
        timeSheet -> {
          final User user = timeSheet.getEmployee();

          final List<EmployeeTimeLog> workedMinutes =
              attendanceMyHoursService.findAllRelevantTimelogs(timeSheet, companyTaSetting);
          final Map<Double, Integer> overtimeMinutes =
              overtimeService.findAllOvertimeHours(workedMinutes, timeSheet, companyTaSetting);

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
              attendanceMyHoursService.getTotalNumberOfWorkedMinutes(workedMinutes, timeSheet);

          final TimePeriod timePeriod = timeSheet.getTimePeriod();

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
                      timeSheet
                          .getStatus()
                          .getName()
                          .equals(StaticTimesheetStatus.TimeSheetStatus.APPROVED.name()))
                  .build());
        });
    return employeeAttendanceSummaryDtoList;
  }

  private List<TimeSheet> findTimeSheetsByIdAndCompanyIdAndStatusAndType(
      final String userId,
      final String timePeriodId,
      final List<String> timeSheetStatus,
      final String hourType) {
    if (hourType.equals(TEAM_HOURS_TYPE)) {
      return timeSheetService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
          userId, timePeriodId, timeSheetStatus);
    }
    return timeSheetService.findCompanyTimeSheetsByIdAndStatus(
        userId, timePeriodId, timeSheetStatus);
  }

  public TeamHoursPageInfoDto findTeamTimeSheetsByIdAndCompanyIdAndStatus(
      final String timePeriodId,
      final String hourType,
      final TimeSheetStatus timeSheetStatus,
      final String userId,
      final Pageable pageable) {
    Page<TimeSheet> timeSheetPage = new PageImpl<>(Collections.emptyList());
    if (hourType.equals(TEAM_HOURS_TYPE)) {
      timeSheetPage =
          timeSheetService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
              timePeriodId, timeSheetStatus, userId, pageable);
    }
    if (hourType.equals(COMPANY_HOURS_TYPE)) {
      timeSheetPage =
          timeSheetService.findCompanyTimeSheetsByIdAndStatus(
              timePeriodId, timeSheetStatus, userId, pageable);
    }
    final CompanyTaSetting companyTaSetting = attendanceSettingsService.findCompanySetting();

    final List<AttendanceTeamHoursDto> teamHoursDtos =
        timeSheetPage.getContent().stream()
            .map(
                timeSheet -> {
                  final List<EmployeeTimeLog> workedMinutes =
                      attendanceMyHoursService.findAllRelevantTimelogs(timeSheet, companyTaSetting);

                  final int workedMin =
                      attendanceMyHoursService.getTotalNumberOfWorkedMinutes(
                          workedMinutes, timeSheet);
                  int totalOvertimeMin = 0;
                  final Map<Double, Integer> overtimeMinutes =
                      overtimeService.findAllOvertimeHours(
                          workedMinutes, timeSheet, companyTaSetting);
                  for (final Map.Entry<Double, Integer> overtimeMinute :
                      overtimeMinutes.entrySet()) {
                    totalOvertimeMin += overtimeMinute.getValue();
                  }
                  return new AttendanceTeamHoursDto(
                      timeSheet.getId(),
                      timeSheet.getEmployee().getId(),
                      timeSheet.getEmployee().getUserPersonalInformation().getName(),
                      workedMin,
                      totalOvertimeMin);
                })
            .collect(Collectors.toList());
    return new TeamHoursPageInfoDto(teamHoursDtos, timeSheetPage.getTotalPages());
  }

  public AttendanceSummaryDto findTeamHoursSummary(
      final String timePeriodId, final String userId, final String hourType) {
    List<TimeSheet> timeSheets = new ArrayList<>();
    if (hourType.equals(TEAM_HOURS_TYPE)) {
      timeSheets =
          timeSheetService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
              userId,
              timePeriodId,
              Arrays.asList(
                  StaticTimesheetStatus.TimeSheetStatus.SUBMITTED.name(),
                  StaticTimesheetStatus.TimeSheetStatus.APPROVED.name()));
    }
    if (hourType.equals(COMPANY_HOURS_TYPE)) {
      timeSheets =
          timeSheetService.findCompanyTimeSheetsByIdAndStatus(
              userId,
              timePeriodId,
              Arrays.asList(
                  StaticTimesheetStatus.TimeSheetStatus.SUBMITTED.name(),
                  StaticTimesheetStatus.TimeSheetStatus.APPROVED.name()));
    }
    final CompanyTaSetting companyTaSetting = attendanceSettingsService.findCompanySetting();
    int totalTimeOffHours = 0;
    int totalOvertimeMin = 0;
    int totalRegularMin = 0;
    for (final TimeSheet timeSheet : timeSheets) {
      final User user = timeSheet.getEmployee();
      final Timestamp timeSheetStart = timeSheet.getTimePeriod().getStartDate();
      final Timestamp timesheetEnd = timeSheet.getTimePeriod().getEndDate();
      final int timeOffHours =
          timeOffRequestService.findTimeOffHoursBetweenWorkPeriod(
              user, timeSheetStart.getTime(), timesheetEnd.getTime());
      totalTimeOffHours += timeOffHours;

      final List<EmployeeTimeLog> workedMinutes =
          attendanceMyHoursService.findAllRelevantTimelogs(timeSheet, companyTaSetting);

      final int workedMin =
          attendanceMyHoursService.getTotalNumberOfWorkedMinutes(workedMinutes, timeSheet);

      final Map<Double, Integer> overtimeMinutes =
          overtimeService.findAllOvertimeHours(workedMinutes, timeSheet, companyTaSetting);
      int currentOvertimeMin = 0;
      for (final Map.Entry<Double, Integer> overtimeMinute : overtimeMinutes.entrySet()) {
        currentOvertimeMin += overtimeMinute.getValue();
      }

      final String compensationFrequency =
          timeSheet.getUserCompensation().getCompensationFrequency().getName();
      if (compensationFrequency.equals(HOUR_TYPE)) {
        totalRegularMin += workedMin - currentOvertimeMin;
      }
      totalOvertimeMin += currentOvertimeMin;
    }
    return AttendanceSummaryDto.builder()
        .workedMinutes(totalRegularMin)
        .overTimeMinutes(totalOvertimeMin)
        .totalPtoMinutes(totalTimeOffHours * CONVERT_HOUR_TO_MIN)
        .build();
  }

  public void approvePendingHours(final Set<String> selectedTimesheets) {
    final StaticTimesheetStatus timesheetApproveStatus =
        staticTimesheetStatusService.findByName(TimeSheetStatus.APPROVED.name());
    final List<TimeSheet> timeSheets = timeSheetService.findAllById(selectedTimesheets);
    timeSheets.forEach(timeSheet -> timeSheet.setStatus(timesheetApproveStatus));
    timeSheetService.saveAll(timeSheets);
  }

  public AttendanceDetailDto findAttendanceDetails(final String companyId) {
    final PayrollDetail payrollDetail = payrollDetailService.findByCompanyId(companyId);
    final TimePeriod timePeriod = timePeriodService.findCompanyCurrentPeriod(companyId);
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
  public void removeAttendanceDetails(final List<String> userIds, final String companyId) {
    employeesTaSettingService.removeEmployees(userIds);
    timeSheetService.removeUserFromAttendance(userIds, companyId);
  }

  public PendingCountDto findAttendancePendingCount(final String userId, final String companyId) {
    final TimePeriod timePeriod = timePeriodService.findCompanyCurrentPeriod(companyId);
    final int teamHoursPendingCount =
        timeSheetService.findTeamHoursPendingCount(userId, timePeriod.getId());
    final int companyHoursPendingCount =
        timeSheetService.findCompanyHoursPendingCount(timePeriod.getId());
    return PendingCountDto.builder()
        .teamHoursPendingCount(teamHoursPendingCount)
        .companyHoursPendingCount(companyHoursPendingCount)
        .build();
  }
}
