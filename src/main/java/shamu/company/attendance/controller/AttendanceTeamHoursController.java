package shamu.company.attendance.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import shamu.company.attendance.dto.AttendanceSummaryDto;
import shamu.company.attendance.dto.TeamHoursPageInfoDto;
import shamu.company.attendance.dto.TimeSheetPeriodDto;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.service.AttendanceTeamHoursService;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.utils.ReflectionUtil;

import java.util.List;

@RestApiController
public class AttendanceTeamHoursController extends BaseRestController {

  private final AttendanceTeamHoursService attendanceTeamHoursService;

  private final TimePeriodService timePeriodService;

  public AttendanceTeamHoursController(
          final AttendanceTeamHoursService attendanceTeamHoursService,
          final TimePeriodService timePeriodService) {
    this.attendanceTeamHoursService = attendanceTeamHoursService;
    this.timePeriodService = timePeriodService;
  }

  @GetMapping("time-and-attendance/team-hours/pending-hours/{timePeriodId}")
  public TeamHoursPageInfoDto findAttendanceTeamPendingHours(
      @PathVariable final String timePeriodId,
      @RequestParam(value = "page") final Integer page,
      @RequestParam(value = "size", defaultValue = "5") final Integer size) {
    final Pageable pageable = PageRequest.of(page - 1, size);

    return attendanceTeamHoursService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
        timePeriodId, findCompanyId(), TimeSheetStatus.SUBMITTED, findUserId(), pageable);
  }

  @GetMapping("time-and-attendance/team-hours/approved-hours/{timePeriodId}")
  public TeamHoursPageInfoDto findAttendanceTeamApprovedHours(
      @PathVariable final String timePeriodId,
      @RequestParam(value = "page") final Integer page,
      @RequestParam(value = "size", defaultValue = "20") final Integer size) {
    final Pageable pageable = PageRequest.of(page - 1, size);

    return attendanceTeamHoursService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
        timePeriodId, findCompanyId(), TimeSheetStatus.APPROVED, findUserId(), pageable);
  }

  @GetMapping("time-and-attendance/team-hours/total-time-off/{timePeriodId}")
  public AttendanceSummaryDto findTeamHoursSummary(@PathVariable final String timePeriodId) {
    return attendanceTeamHoursService.findTeamHoursSummary(
        timePeriodId, findCompanyId(), findUserId());
  }

  @GetMapping("time-and-attendance/time-periods")
  public List<TimeSheetPeriodDto> findTimePeriodsByCompany() {
    return ReflectionUtil.convertTo(
        timePeriodService.listByCompany(findCompanyId()), TimeSheetPeriodDto.class);
  }
}
