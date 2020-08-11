package shamu.company.attendance.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import shamu.company.attendance.dto.AttendanceSummaryDto;
import shamu.company.attendance.dto.EmployeeInfoDto;
import shamu.company.attendance.dto.TeamHoursPageInfoDto;
import shamu.company.attendance.dto.TimeSheetPeriodDto;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.service.AttendanceSettingsService;
import shamu.company.attendance.service.AttendanceTeamHoursService;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.job.service.JobUserService;
import shamu.company.utils.ReflectionUtil;

import java.util.List;
import java.util.Set;

@RestApiController
public class AttendanceTeamHoursController extends BaseRestController {

  private final AttendanceTeamHoursService attendanceTeamHoursService;

  private final TimePeriodService timePeriodService;

  private final JobUserService jobUserService;

  private final AttendanceSettingsService attendanceSettingsService;

  public AttendanceTeamHoursController(
      final AttendanceTeamHoursService attendanceTeamHoursService,
      final TimePeriodService timePeriodService,
      final JobUserService jobUserService,
      final AttendanceSettingsService attendanceSettingsService) {
    this.attendanceTeamHoursService = attendanceTeamHoursService;
    this.timePeriodService = timePeriodService;
    this.jobUserService = jobUserService;
    this.attendanceSettingsService = attendanceSettingsService;
  }

  @GetMapping("time-and-attendance/team-hours/pending-hours/{timePeriodId}/{hourType}")
  public TeamHoursPageInfoDto findAttendanceTeamPendingHours(
      @PathVariable final String timePeriodId,
      @PathVariable final String hourType,
      @RequestParam(value = "page") final Integer page,
      @RequestParam(value = "size", defaultValue = "5") final Integer size) {
    final Pageable pageable = PageRequest.of(page - 1, size);

    return attendanceTeamHoursService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
        timePeriodId, hourType, findCompanyId(), TimeSheetStatus.SUBMITTED, findUserId(), pageable);
  }

  @GetMapping("time-and-attendance/team-hours/approved-hours/{timePeriodId}/{hourType}")
  public TeamHoursPageInfoDto findAttendanceTeamApprovedHours(
      @PathVariable final String timePeriodId,
      @PathVariable final String hourType,
      @RequestParam(value = "page") final Integer page,
      @RequestParam(value = "size", defaultValue = "20") final Integer size) {
    final Pageable pageable = PageRequest.of(page - 1, size);

    return attendanceTeamHoursService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
        timePeriodId, hourType, findCompanyId(), TimeSheetStatus.APPROVED, findUserId(), pageable);
  }

  @GetMapping("time-and-attendance/team-hours-summary/{timePeriodId}/{hourType}")
  public AttendanceSummaryDto findTeamHoursSummary(
      @PathVariable final String timePeriodId, @PathVariable final String hourType) {
    return attendanceTeamHoursService.findTeamHoursSummary(
        timePeriodId, findCompanyId(), findUserId(), hourType);
  }

  @GetMapping("time-and-attendance/time-periods")
  public List<TimeSheetPeriodDto> findTimePeriodsByCompany() {
    return ReflectionUtil.convertTo(
        timePeriodService.listByCompany(findCompanyId()), TimeSheetPeriodDto.class);
  }

  @PatchMapping("time-and-attendance/team-hours/pending-hours/approved")
  public HttpEntity<String> approvePendingHours(@RequestBody final Set<String> selectedTimesheets) {
    attendanceTeamHoursService.approvePendingHours(selectedTimesheets);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("time-and-attendance/team-hours/employee-info/{userId}")
  public EmployeeInfoDto findEmployeeInfo(@PathVariable final String userId) {
    return jobUserService.findEmployeeInfo(userId);
  }

  @GetMapping("time-and-attendance/approval-days-before-payroll/{userId}")
  public int findApprovalDaysBeforePayroll(@PathVariable final String userId) {
    return attendanceSettingsService.findApprovalDaysBeforePayroll(userId);
  }
}
