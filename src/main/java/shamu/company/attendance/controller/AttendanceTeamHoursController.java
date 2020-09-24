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
import shamu.company.attendance.dto.AttendanceDetailDto;
import shamu.company.attendance.dto.AttendancePolicyAndDetailDto;
import shamu.company.attendance.dto.AttendanceSummaryDto;
import shamu.company.attendance.dto.EmployeeAttendanceSummaryDto;
import shamu.company.attendance.dto.EmployeeInfoDto;
import shamu.company.attendance.dto.PendingCountDto;
import shamu.company.attendance.dto.TeamHoursPageInfoDto;
import shamu.company.attendance.dto.TimeAndAttendanceDetailsDto;
import shamu.company.attendance.dto.TimeSheetPeriodDto;
import shamu.company.attendance.entity.StaticTimesheetStatus.TimeSheetStatus;
import shamu.company.attendance.service.AttendanceSetUpService;
import shamu.company.attendance.service.AttendanceSettingsService;
import shamu.company.attendance.service.AttendanceTeamHoursService;
import shamu.company.attendance.service.TimePeriodService;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.job.service.JobUserService;
import shamu.company.utils.ReflectionUtil;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestApiController
public class AttendanceTeamHoursController extends BaseRestController {

  private static final String COMPANY_HOURS_TYPE = "company_hours";

  private final AttendanceTeamHoursService attendanceTeamHoursService;

  private final TimePeriodService timePeriodService;

  private final JobUserService jobUserService;

  private final AttendanceSettingsService attendanceSettingsService;

  private final AttendanceSetUpService attendanceSetUpService;

  public AttendanceTeamHoursController(
      final AttendanceTeamHoursService attendanceTeamHoursService,
      final TimePeriodService timePeriodService,
      final JobUserService jobUserService,
      final AttendanceSettingsService attendanceSettingsService,
      final AttendanceSetUpService attendanceSetUpService) {
    this.attendanceTeamHoursService = attendanceTeamHoursService;
    this.timePeriodService = timePeriodService;
    this.jobUserService = jobUserService;
    this.attendanceSettingsService = attendanceSettingsService;
    this.attendanceSetUpService = attendanceSetUpService;
  }

  @GetMapping("time-and-attendance/team-hours/pending-hours/{timePeriodId}/{hourType}")
  public TeamHoursPageInfoDto findAttendanceTeamPendingHours(
      @PathVariable final String timePeriodId,
      @PathVariable final String hourType,
      @RequestParam(value = "page") final Integer page,
      @RequestParam(value = "size", defaultValue = "5") final Integer size) {
    final Pageable pageable = PageRequest.of(page - 1, size);

    return attendanceTeamHoursService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
        timePeriodId,
        hourType,
        Collections.singletonList(TimeSheetStatus.SUBMITTED.getValue()),
        findUserId(),
        pageable);
  }

  @GetMapping("time-and-attendance/team-hours/{timePeriodId}/{hourType}")
  public List<EmployeeAttendanceSummaryDto> findTeamEmployeesHours(
      @PathVariable final String timePeriodId, @PathVariable final String hourType) {
    return attendanceTeamHoursService.findEmployeeAttendanceSummary(
        timePeriodId, findUserId(), hourType);
  }

  @GetMapping("time-and-attendance/team-hours/approved-hours/{timePeriodId}/{hourType}")
  public TeamHoursPageInfoDto findAttendanceTeamApprovedHours(
      @PathVariable final String timePeriodId,
      @PathVariable final String hourType,
      @RequestParam(value = "page") final Integer page,
      @RequestParam(value = "size", defaultValue = "20") final Integer size) {
    final Pageable pageable = PageRequest.of(page - 1, size);

    return attendanceTeamHoursService.findTeamTimeSheetsByIdAndCompanyIdAndStatus(
        timePeriodId,
        hourType,
        Arrays.asList(
            TimeSheetStatus.APPROVED.getValue(),
            TimeSheetStatus.COMPLETE.getValue(),
            TimeSheetStatus.SEND_TO_PAYROLL.getValue()),
        findUserId(),
        pageable);
  }

  @GetMapping("time-and-attendance/team-hours-summary/{timePeriodId}/{hourType}")
  public AttendanceSummaryDto findTeamHoursSummary(
      @PathVariable final String timePeriodId, @PathVariable final String hourType) {
    return attendanceTeamHoursService.findTeamHoursSummary(
        timePeriodId,
        Arrays.asList(
            TimeSheetStatus.SUBMITTED.name(),
            TimeSheetStatus.APPROVED.name(),
            TimeSheetStatus.COMPLETE.name(),
            TimeSheetStatus.SEND_TO_PAYROLL.name()),
        findUserId(),
        hourType);
  }

  @GetMapping("time-and-attendance/time-periods")
  public List<TimeSheetPeriodDto> findTimePeriodsByCompany() {
    return ReflectionUtil.convertTo(timePeriodService.findAll(), TimeSheetPeriodDto.class);
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

  @GetMapping("time-and-attendance/approval-days-before-payroll")
  public int findApprovalDaysBeforePayroll() {
    return attendanceSettingsService.findApprovalDaysBeforePayroll();
  }

  @GetMapping("time-and-attendance/attendance-details")
  public AttendanceDetailDto findAttendanceDetails() {
    return attendanceTeamHoursService.findAttendanceDetails();
  }

  @PatchMapping("time-and-attendance/details")
  public HttpEntity updateAttendanceDetails(
      @Valid @RequestBody final AttendancePolicyAndDetailDto attendancePolicyAndDetailDto) {
    final TimeAndAttendanceDetailsDto timeAndAttendanceDetailsDto =
        attendancePolicyAndDetailDto.getAttendanceDetails();
    final String companyId = findCompanyId();
    final String employeeId = findUserId();
    if (!timeAndAttendanceDetailsDto.getOvertimeDetails().isEmpty()) {
      attendanceSetUpService.saveAttendanceDetails(
          attendancePolicyAndDetailDto, companyId, employeeId);
    }
    if (!timeAndAttendanceDetailsDto.getRemovedUserIds().isEmpty()) {
      attendanceTeamHoursService.removeAttendanceDetails(
          timeAndAttendanceDetailsDto.getRemovedUserIds());
    }
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping("time-and-attendance/pending-count")
  public PendingCountDto findAttendancePendingCount() {
    return attendanceTeamHoursService.findAttendancePendingCount(findAuthUser().getId());
  }

  @GetMapping("time-and-attendance/approved-company-hours-summary/{timePeriodId}")
  public AttendanceSummaryDto findApprovedAttendanceSummary(
      @PathVariable final String timePeriodId) {
    return attendanceTeamHoursService.findTeamHoursSummary(
        timePeriodId,
        Collections.singletonList(TimeSheetStatus.APPROVED.name()),
        findUserId(),
        COMPANY_HOURS_TYPE);
  }

  @PatchMapping("time-and-attendance/complete-period/{timePeriodId}")
  public void completePeriod(@PathVariable final String timePeriodId) {
    attendanceTeamHoursService.completePeriod(timePeriodId);
  }
}
