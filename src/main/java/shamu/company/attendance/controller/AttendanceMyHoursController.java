package shamu.company.attendance.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.attendance.dto.AllTimeEntryDto;
import shamu.company.attendance.dto.AttendanceSummaryDto;
import shamu.company.attendance.dto.TimeEntryDto;
import shamu.company.attendance.dto.TimeSheetPeriodDto;
import shamu.company.attendance.dto.UserAttendanceEnrollInfoDto;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.service.AttendanceMyHoursService;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.employee.dto.CompensationDto;
import shamu.company.utils.ReflectionUtil;

import java.util.List;

@RestApiController
public class AttendanceMyHoursController extends BaseRestController {

  private final AttendanceMyHoursService attendanceMyHoursService;
  private final TimePeriodService timePeriodService;
  private final AttendanceSetUpService attendanceSetUpService;

  public AttendanceMyHoursController(
      final AttendanceMyHoursService attendanceMyHoursService,
      final TimePeriodService timePeriodService,
      final AttendanceSetUpService attendanceSetUpService) {
    this.attendanceMyHoursService = attendanceMyHoursService;
    this.timePeriodService = timePeriodService;
    this.attendanceSetUpService = attendanceSetUpService;
  }

  @PostMapping("time-and-attendance/{userId}/add-time-entries")
  @PreAuthorize(
      "hasPermission(#userId,'USER', 'EDIT_SELF')"
          + " or hasPermission(#userId,'USER', 'MANAGE_TEAM_USER')")
  public void saveTimeEntry(
      @PathVariable final String userId, @RequestBody final TimeEntryDto timeEntryDto) {
    attendanceMyHoursService.saveTimeEntry(userId, timeEntryDto, findUserId());
  }

  @GetMapping("time-and-attendance/all-paid-time/{timesheetId}")
  @PreAuthorize(
      "hasPermission(#timesheetId,'ATTENDANCE_TIMESHEET', 'VIEW_EMPLOYEES')"
          + " or hasAuthority('VIEW_SELF')")
  public List<AllTimeEntryDto> findAllHours(@PathVariable final String timesheetId) {
    return attendanceMyHoursService.findAllHours(timesheetId);
  }

  @GetMapping("time-and-attendance/user-compensation/{userId}")
  @PreAuthorize(
      "hasPermission(#userId, 'USER', 'MANAGE_TEAM_USER')"
          + " or hasPermission(#userId,'USER', 'VIEW_SELF')")
  public CompensationDto findUserCompensation(@PathVariable final String userId) {
    return attendanceMyHoursService.findUserCompensation(userId);
  }

  @GetMapping("time-and-attendance/attendance-summary/{timesheetId}")
  @PreAuthorize(
      "hasPermission(#timesheetId,'ATTENDANCE_TIMESHEET', 'VIEW_EMPLOYEES')"
          + " or hasAuthority('VIEW_SELF')")
  public AttendanceSummaryDto findAttendanceSummary(@PathVariable final String timesheetId) {
    return attendanceMyHoursService.findAttendanceSummary(timesheetId);
  }

  @GetMapping("time-and-attendance/user-timezone/{userId}")
  @PreAuthorize("hasPermission(#userId, 'USER', 'VIEW_SELF')")
  public String findUserTimeZone(@PathVariable final String userId) {
    return attendanceMyHoursService.findUserTimeZone(userId);
  }

  @GetMapping("time-and-attendance/time-periods/{userId}")
  @PreAuthorize("hasPermission(#userId, 'USER', 'VIEW_SELF')")
  public List<TimeSheetPeriodDto> findTimePeriodsByUser(@PathVariable final String userId) {
    return ReflectionUtil.convertTo(timePeriodService.listByUser(userId), TimeSheetPeriodDto.class);
  }

  @GetMapping("time-and-attendance/{userId}/next-time-period")
  @PreAuthorize(
      "hasPermission(#userId, 'USER', 'EDIT_SELF')"
          + " or hasPermission(#userId,'USER', 'EDIT_USER')")
  public TimePeriod findNextTimePeriodByUser(@PathVariable final String userId) {
    return attendanceSetUpService.findNextPeriodByUser(userId);
  }

  @GetMapping("time-and-attendance/entry/{entryId}")
  @PreAuthorize(
      "hasPermission(#entryId,'ATTENDANCE_ENTRY', 'VIEW_EMPLOYEES')"
          + " or hasAuthority('VIEW_SELF')")
  public TimeEntryDto findMyHourEntry(@PathVariable final String entryId) {
    return attendanceMyHoursService.findMyHourEntry(entryId);
  }

  @PatchMapping("time-and-attendance/my-hours/submit-for-approval/{timesheetId}")
  @PreAuthorize("hasPermission(#timesheetId,'ATTENDANCE_TIMESHEET', 'EDIT_SELF')")
  public void submitHoursForApproval(@PathVariable final String timesheetId) {
    attendanceMyHoursService.submitHoursForApproval(timesheetId);
  }

  @GetMapping("time-and-attendance/my-hours/timesheet-status/{timesheetId}")
  @PreAuthorize(
      "hasPermission(#timesheetId,'ATTENDANCE_TIMESHEET', 'VIEW_EMPLOYEES')"
          + " or hasAuthority('VIEW_SELF')")
  public String findTimesheetStatus(@PathVariable final String timesheetId) {
    return attendanceMyHoursService.findTimesheetStatus(timesheetId);
  }

  @DeleteMapping("time-and-attendance/my-hours/time-entry/{entryId}")
  @PreAuthorize("hasPermission(#entryId,'ATTENDANCE_ENTRY', 'MANAGE_EMPLOYEES')")
  public void deleteMyHourEntry(@PathVariable final String entryId) {
    attendanceMyHoursService.deleteMyHourEntry(entryId, findUserId());
  }

  @GetMapping("time-and-attendance/user-in-attendance/{userId}")
  @PreAuthorize(
      "hasPermission(#userId, 'USER', 'MANAGE_TEAM_USER')"
          + " or hasPermission(#userId,'USER', 'VIEW_SELF')")
  public UserAttendanceEnrollInfoDto findUserAttendanceEnrollInfo(
      @PathVariable final String userId) {
    return attendanceMyHoursService.findUserAttendanceEnrollInfo(userId);
  }
}
