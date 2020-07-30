package shamu.company.attendance.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import shamu.company.attendance.dto.AttendanceSummaryDto;
import shamu.company.attendance.dto.TeamHoursPageInfoDto;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.service.AttendanceTeamHoursService;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;

@RestApiController
public class AttendanceTeamHoursController extends BaseRestController {

  private final AttendanceTeamHoursService attendanceTeamHoursService;

  public AttendanceTeamHoursController(
      final AttendanceTeamHoursService attendanceTeamHoursService) {
    this.attendanceTeamHoursService = attendanceTeamHoursService;
  }

  @GetMapping("time-and-attendance/team-hours/pending-hours/{timesheetId}")
  public TeamHoursPageInfoDto findAttendanceTeamPendingHours(
      @PathVariable final String timesheetId,
      @RequestParam(value = "page") final Integer page,
      @RequestParam(value = "size", defaultValue = "5") final Integer size) {
    final Pageable pageable = PageRequest.of(page - 1, size);

    return attendanceTeamHoursService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
        timesheetId, findCompanyId(), TimeSheetStatus.SUBMITTED, findUserId(), pageable);
  }

  @GetMapping("time-and-attendance/team-hours/approved-hours/{timesheetId}")
  public TeamHoursPageInfoDto findAttendanceTeamApprovedHours(
      @PathVariable final String timesheetId,
      @RequestParam(value = "page") final Integer page,
      @RequestParam(value = "size", defaultValue = "20") final Integer size) {
    final Pageable pageable = PageRequest.of(page - 1, size);

    return attendanceTeamHoursService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
        timesheetId, findCompanyId(), TimeSheetStatus.APPROVED, findUserId(), pageable);
  }

  @GetMapping("time-and-attendance/team-hours/total-time-off/{timesheetId}")
  public AttendanceSummaryDto findTeamHoursSummary(@PathVariable final String timesheetId) {
    return attendanceTeamHoursService.findTeamHoursSummary(
        timesheetId, findCompanyId(), findUserId());
  }
}