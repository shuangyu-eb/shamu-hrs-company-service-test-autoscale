package shamu.company.attendance.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.attendance.dto.AttendanceTeamHoursDto;
import shamu.company.attendance.dto.TeamHoursPageInfoDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.entity.TimeSheet;

@Transactional
@Service
public class AttendanceTeamHoursService {

  private final TimeSheetService timeSheetService;
  private final AttendanceMyHoursService attendanceMyHoursService;
  private final AttendanceSettingsService attendanceSettingsService;
  private final OvertimeService overtimeService;

  public AttendanceTeamHoursService(
      final TimeSheetService timeSheetService,
      final AttendanceMyHoursService attendanceMyHoursService,
      final AttendanceSettingsService attendanceSettingsService,
      final OvertimeService overtimeService) {
    this.timeSheetService = timeSheetService;
    this.attendanceMyHoursService = attendanceMyHoursService;
    this.attendanceSettingsService = attendanceSettingsService;
    this.overtimeService = overtimeService;
  }

  public TeamHoursPageInfoDto findTeamTimeSheetsByIdAndCompanyIdAndStatus(
      final String timesheetId, final String companyId, final TimeSheetStatus timeSheetStatus, final
      Pageable pageable) {
    Page<TimeSheet> timeSheetPage =
        timeSheetService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
            timesheetId, companyId, timeSheetStatus, pageable);
    final CompanyTaSetting companyTaSetting =
        attendanceSettingsService.findCompanySettings(companyId);

    List<AttendanceTeamHoursDto> teamHoursDtos = timeSheetPage.getContent().stream()
        .map(
            timeSheet -> {
              final List<EmployeeTimeLog> workedMinutes =
                  attendanceMyHoursService.findAllRelevantTimelogs(timeSheet, companyTaSetting);

              final int workedMin =
                  attendanceMyHoursService.getTotalNumberOfWorkedMinutes(workedMinutes, timeSheet);
              int totalOvertimeMin = 0;
              final Map<Double, Integer> overtimeMinutes =
                  overtimeService.findAllOvertimeHours(workedMinutes, timeSheet, companyTaSetting);
              for (final Map.Entry<Double, Integer> overtimeMinute : overtimeMinutes.entrySet()) {
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
}
