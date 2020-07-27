package shamu.company.attendance.service;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.dto.AttendanceSummaryDto;
import shamu.company.attendance.dto.AttendanceTeamHoursDto;
import shamu.company.attendance.dto.TeamHoursPageInfoDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.StaticTimesheetStatus;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.timeoff.service.TimeOffRequestService;
import shamu.company.user.entity.User;

@Transactional
@Service
public class AttendanceTeamHoursService {
  private static final int CONVERT_HOUR_TO_MIN = 60;
  private static final String HOUR_TYPE = "Per Hour";

  private final TimeSheetService timeSheetService;
  private final AttendanceMyHoursService attendanceMyHoursService;
  private final AttendanceSettingsService attendanceSettingsService;
  private final OvertimeService overtimeService;
  private final TimeOffRequestService timeOffRequestService;

  public AttendanceTeamHoursService(
      final TimeSheetService timeSheetService,
      final AttendanceMyHoursService attendanceMyHoursService,
      final AttendanceSettingsService attendanceSettingsService,
      final OvertimeService overtimeService,
      final TimeOffRequestService timeOffRequestService) {
    this.timeSheetService = timeSheetService;
    this.attendanceMyHoursService = attendanceMyHoursService;
    this.attendanceSettingsService = attendanceSettingsService;
    this.overtimeService = overtimeService;
    this.timeOffRequestService = timeOffRequestService;
  }

  public TeamHoursPageInfoDto findTeamTimeSheetsByIdAndCompanyIdAndStatus(
      final String timesheetId,
      final String companyId,
      final TimeSheetStatus timeSheetStatus,
      final String userId,
      final Pageable pageable) {
    final Page<TimeSheet> timeSheetPage =
        timeSheetService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
            timesheetId, companyId, timeSheetStatus, userId, pageable);
    final CompanyTaSetting companyTaSetting =
        attendanceSettingsService.findCompanySettings(companyId);

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
      final String timesheetId, final String companyId, final String userId) {
    final List<TimeSheet> timeSheets =
        timeSheetService.findTimeSheetsByIdAndCompanyIdAndStatus(
            userId,
            timesheetId,
            companyId,
            Arrays.asList(
                StaticTimesheetStatus.TimeSheetStatus.SUBMITTED.name(),
                StaticTimesheetStatus.TimeSheetStatus.APPROVED.name()));
    final CompanyTaSetting companyTaSetting =
        attendanceSettingsService.findCompanySettings(companyId);
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
}
