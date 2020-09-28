package shamu.company.attendance.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.attendance.dto.CompanyTaSettingsDto;
import shamu.company.attendance.dto.EmployeeOvertimeDetailsDto;
import shamu.company.attendance.dto.EmployeesTaSettingDto;
import shamu.company.attendance.dto.NewOvertimePolicyDto;
import shamu.company.attendance.dto.OvertimePolicyDto;
import shamu.company.attendance.dto.OvertimePolicyOverviewDto;
import shamu.company.attendance.dto.StaticTimezoneDto;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.attendance.entity.mapper.CompanyTaSettingsMapper;
import shamu.company.attendance.entity.mapper.EmployeesTaSettingsMapper;
import shamu.company.attendance.service.AttendanceSettingsService;
import shamu.company.attendance.service.OvertimeService;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.common.service.PayrollDetailService;
import shamu.company.utils.ReflectionUtil;

import java.util.List;

@RestApiController
public class AttendanceSettingsController extends BaseRestController {

  final AttendanceSettingsService attendanceSettingsService;

  final CompanyTaSettingsMapper companyTaSettingsMapper;

  final EmployeesTaSettingsMapper employeesTaSettingsMapper;

  final PayrollDetailService payrollDetailService;

  final OvertimeService overtimeService;

  public AttendanceSettingsController(
      final AttendanceSettingsService attendanceSettingsService,
      final CompanyTaSettingsMapper companyTaSettingsMapper,
      final EmployeesTaSettingsMapper employeesTaSettingsMapper,
      final PayrollDetailService payrollDetailService,
      final OvertimeService overtimeService) {
    this.attendanceSettingsService = attendanceSettingsService;
    this.companyTaSettingsMapper = companyTaSettingsMapper;
    this.employeesTaSettingsMapper = employeesTaSettingsMapper;
    this.payrollDetailService = payrollDetailService;
    this.overtimeService = overtimeService;
  }

  @GetMapping("time-and-attendance/company-settings")
  @PreAuthorize("hasAuthority('MANAGE_COMPANY_USER')")
  public CompanyTaSettingsDto findCompanySettings() {
    return companyTaSettingsMapper.convertToCompanyTaSettingsDto(
        attendanceSettingsService.findCompanySetting(), payrollDetailService.find());
  }

  @GetMapping("time-and-attendance/employee-settings/{employeeId}")
  @PreAuthorize("hasPermission(#userId, 'USER', 'VIEW_SELF')")
  public EmployeesTaSettingDto findEmployeeSettings(@PathVariable final String employeeId) {
    return attendanceSettingsService.findEmployeesSettings(employeeId);
  }

  @GetMapping("time-and-attendance/static-timezones")
  public List<StaticTimezoneDto> findAllTimeZones() {
    final List<StaticTimezone> staticTimezones = attendanceSettingsService.findAllStaticTimeZones();
    return ReflectionUtil.convertTo(staticTimezones, StaticTimezoneDto.class);
  }

  @PatchMapping("time-and-attendance/company-settings")
  @PreAuthorize("hasAuthority('MANAGE_COMPANY_USER')")
  public HttpEntity<String> updateCompanySettings(
      @RequestBody final CompanyTaSettingsDto companyTaSettingsDto) {
    attendanceSettingsService.updateCompanySettings(companyTaSettingsDto);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PatchMapping("time-and-attendance/{employeeId}/employee-settings")
  @PreAuthorize("hasPermission(#userId, 'USER', 'EDIT_SELF')")
  public HttpEntity<String> updateEmployeeSettings(
      @PathVariable final String employeeId,
      @RequestBody final EmployeesTaSettingDto employeesTaSettingDto) {
    attendanceSettingsService.updateEmployeeSettings(employeeId, employeesTaSettingDto);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("time-and-attendance/{employeeId}/is-in-attendance")
  public boolean isInAttendance(@PathVariable final String employeeId) {
    return attendanceSettingsService.findEmployeeIsAttendanceSetUp(employeeId);
  }

  @PostMapping("time-and-attendance/create-overtime-policy")
  @PreAuthorize("hasAuthority('MANAGE_COMPANY_USER')")
  public HttpEntity<String> createOvertimePolicy(
      @RequestBody final NewOvertimePolicyDto overtimePolicy) {
    overtimeService.saveNewOvertimePolicy(overtimePolicy);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PatchMapping("time-and-attendance/overtime-policy")
  @PreAuthorize("hasAuthority('MANAGE_COMPANY_USER')")
  public HttpEntity<String> updateOvertimePolicy(
      @RequestBody final OvertimePolicyDto overtimePolicy) {
    overtimeService.updateOvertimePolicy(overtimePolicy);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("time-and-attendance/overtime-policies")
  @PreAuthorize("hasAuthority('MANAGE_COMPANY_USER')")
  public List<OvertimePolicyOverviewDto> getOvertimePolicies() {
    return overtimeService.findAllOvertimePolicies();
  }

  @GetMapping("time-and-attendance/overtime-policies/{policyId}")
  @PreAuthorize("hasAuthority('MANAGE_COMPANY_USER')")
  public OvertimePolicyDto getOvertimePolicyDetail(@PathVariable final String policyId) {
    return overtimeService.findOvertimePolicyDetails(policyId);
  }

  @DeleteMapping("time-and-attendance/delete-overtime-policy/{policyId}")
  @PreAuthorize("hasAuthority('MANAGE_COMPANY_USER')")
  public HttpEntity<String> deleteOvertimePolicy(@PathVariable final String policyId) {
    overtimeService.softDeleteOvertimePolicy(policyId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("time-and-attendance/all-active-policy-name")
  @PreAuthorize("hasAuthority('MANAGE_COMPANY_USER')")
  public List<String> findAllPolicyNames() {
    return overtimeService.findAllPolicyNames();
  }

  @PatchMapping("time-and-attendance/employee-overtime-policies")
  @PreAuthorize("hasAuthority('MANAGE_COMPANY_USER')")
  public HttpEntity<String> editEmployeeOvertimePolicies(
      @RequestBody final List<EmployeeOvertimeDetailsDto> employeeOvertimeDetailsDtoList) {
    overtimeService.updateEmployeeOvertimePolicies(employeeOvertimeDetailsDtoList);
    return new ResponseEntity(HttpStatus.OK);
  }
}
