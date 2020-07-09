package shamu.company.attendance.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.attendance.dto.AttendanceSummaryDto;
import shamu.company.attendance.dto.MyHoursEntryDto;
import shamu.company.attendance.dto.TimeEntryDto;
import shamu.company.attendance.service.AttendanceMyHoursService;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.employee.dto.CompensationDto;

@RestApiController
public class AttendanceMyHoursController extends BaseRestController {

  private final AttendanceMyHoursService attendanceMyHoursService;

  public AttendanceMyHoursController(final AttendanceMyHoursService attendanceMyHoursService) {
    this.attendanceMyHoursService = attendanceMyHoursService;
  }

  @PostMapping("time-and-attendance/{userId}/add-time-entries")
  public void saveTimeEntry(
      @PathVariable final String userId, @RequestBody final TimeEntryDto timeEntryDto) {
    attendanceMyHoursService.saveTimeEntry(userId, timeEntryDto);
  }

  @GetMapping("time-and-attendance/total-paid-time/{timesheetId}")
  public List<MyHoursEntryDto> findMyHoursList(@PathVariable final String timesheetId) {
    return attendanceMyHoursService.findMyHoursEntries(timesheetId);
  }

  @GetMapping("time-and-attendance/user-compensation/{userId}")
  public CompensationDto findUserCompensation(@PathVariable final String userId) {
    return attendanceMyHoursService.findUserCompensation(userId);
  }

  @GetMapping("time-and-attendance/attendance-summary/{timesheetId}")
  public AttendanceSummaryDto findAttendanceSummary(@PathVariable final String timesheetId) {
    return attendanceMyHoursService.findAttendanceSummary(timesheetId);
  }

  @GetMapping("time-and-attendance/user-timezone/{timesheetId}")
  public String findUserTimeZone(@PathVariable final String timesheetId) {
    return attendanceMyHoursService.findUserTimeZone(timesheetId);
  }
}
