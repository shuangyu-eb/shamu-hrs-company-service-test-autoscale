package shamu.company.attendance.service;

import java.sql.Timestamp;
import java.util.List;
import org.springframework.stereotype.Service;
import shamu.company.attendance.dto.LocalDateEntryDto;
import shamu.company.attendance.dto.OvertimeDetailDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeeTimeLog;
import shamu.company.attendance.entity.TimeSheet;
import shamu.company.attendance.utils.TimeEntryUtils;
import shamu.company.attendance.utils.overtime.OverTimePayFactory;
import shamu.company.server.dto.AuthUser;
import shamu.company.utils.DateUtil;

/** @author mshumaker */
@Service
public class OvertimeService {

  TimeSheetService timeSheetService;
  AttendanceMyHoursService attendanceMyHoursService;
  AttendanceSettingsService attendanceSettingsService;

  public OvertimeService(
      final TimeSheetService timeSheetService,
      final AttendanceMyHoursService attendanceMyHoursService,
      final AttendanceSettingsService attendanceSettingsService) {
    this.timeSheetService = timeSheetService;
    this.attendanceMyHoursService = attendanceMyHoursService;
    this.attendanceSettingsService = attendanceSettingsService;
  }

  public List<OvertimeDetailDto> getOvertimeEntries(final String timesheetId, final AuthUser user) {
    final TimeSheet timeSheet = timeSheetService.findTimeSheetById(timesheetId);
    final CompanyTaSetting companyTaSetting =
        attendanceSettingsService.findCompanySettings(user.getCompanyId());
    final Timestamp timeSheetStart = timeSheet.getTimePeriod().getStartDate();
    final Timestamp timesheetEnd = timeSheet.getTimePeriod().getEndDate();
    final long startOfTimesheetWeek =
        DateUtil.getFirstHourOfWeek(timeSheetStart, companyTaSetting.getTimeZone().getName());
    final String userId = timeSheet.getEmployee().getId();
    final List<EmployeeTimeLog> allEmployeeEntries =
        attendanceMyHoursService.findEntriesBetweenDates(
            startOfTimesheetWeek, timesheetEnd.getTime(), userId, true);
    final List<LocalDateEntryDto> localDateEntries =
        TimeEntryUtils.transformTimeLogsToLocalDate(
            allEmployeeEntries, companyTaSetting.getTimeZone());
    return OverTimePayFactory.getOverTimePay(timeSheet.getUserCompensation())
        .getOvertimePay(localDateEntries);
  }
}
