package shamu.company.attendance.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.attendance.dto.AllTimeEntryDto;
import shamu.company.attendance.dto.AttendanceSummaryDto;
import shamu.company.attendance.dto.TimeEntryDto;
import shamu.company.attendance.dto.TimeSheetPeriodDto;
import shamu.company.attendance.entity.TimePeriod;
import shamu.company.attendance.service.AttendanceMyHoursService;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.employee.dto.CompensationDto;

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
  public void saveTimeEntry(
      @PathVariable final String userId, @RequestBody final TimeEntryDto timeEntryDto) {
    attendanceMyHoursService.saveTimeEntry(userId, timeEntryDto);
  }

  @GetMapping("time-and-attendance/all-paid-time/{timesheetId}")
  public List<AllTimeEntryDto> findAllHours(@PathVariable final String timesheetId) {
    return attendanceMyHoursService.findAllHours(timesheetId);
  }

  @GetMapping("time-and-attendance/user-compensation/{userId}")
  public CompensationDto findUserCompensation(@PathVariable final String userId) {
    return attendanceMyHoursService.findUserCompensation(userId);
  }

  @GetMapping("time-and-attendance/attendance-summary/{timesheetId}")
  public AttendanceSummaryDto findAttendanceSummary(@PathVariable final String timesheetId) {
    return attendanceMyHoursService.findAttendanceSummary(timesheetId);
  }

  @GetMapping("time-and-attendance/user-timezone/{userId}")
  public String findUserTimeZone(@PathVariable final String userId) {
    return attendanceMyHoursService.findUserTimeZone(userId);
  }

  @GetMapping("time-and-attendance/time-periods/{userId}")
  public List<TimeSheetPeriodDto> findTimePeriodsByUser(@PathVariable final String userId) {
    return timePeriodService.listByUser(userId);
  }

  @GetMapping("time-and-attendance/{userId}/next-time-period")
  public TimePeriod findNextTimePeriodByUser(@PathVariable final String userId) {
    return attendanceSetUpService.findNextPeriodByUser(userId);
  }

  @GetMapping("time-and-attendance/compensation-frequency/{timesheetId}")
  public String findCompensationFrequency(@PathVariable final String timesheetId) {
    return attendanceMyHoursService.findCompensationFrequency(timesheetId);
  }

  @GetMapping("time-and-attendance/entry/{entryId}")
  public TimeEntryDto findMyHourEntry(@PathVariable final String entryId) {
    return attendanceMyHoursService.findMyHourEntry(entryId);
  }
}
