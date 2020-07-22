package shamu.company.attendance.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import shamu.company.attendance.dto.CompanyTaSettingsDto;
import shamu.company.attendance.dto.EmployeesTaSettingDto;
import shamu.company.attendance.dto.StaticTimezoneDto;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.attendance.entity.mapper.CompanyTaSettingsMapper;
import shamu.company.attendance.entity.mapper.EmployeesTaSettingsMapper;
import shamu.company.attendance.service.AttendanceSettingsService;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.utils.ReflectionUtil;

@RestApiController
public class AttendanceSettingsController extends BaseRestController {

  final AttendanceSettingsService attendanceSettingsService;

  final CompanyTaSettingsMapper companyTaSettingsMapper;

  final EmployeesTaSettingsMapper employeesTaSettingsMapper;

  public AttendanceSettingsController(
      final AttendanceSettingsService attendanceSettingsService,
      final CompanyTaSettingsMapper companyTaSettingsMapper,
      final EmployeesTaSettingsMapper employeesTaSettingsMapper) {
    this.attendanceSettingsService = attendanceSettingsService;
    this.companyTaSettingsMapper = companyTaSettingsMapper;
    this.employeesTaSettingsMapper = employeesTaSettingsMapper;
  }

  @GetMapping("time-and-attendance/companySettings")
  public CompanyTaSettingsDto findCompanySettings() {
    return companyTaSettingsMapper.convertToCompanyTaSettingsDto(
        attendanceSettingsService.findCompanySettings(findCompanyId()));
  }

  @GetMapping("time-and-attendance/employeeSettings/{employeeId}")
  public EmployeesTaSettingDto findEmployeeSettings(@PathVariable final String employeeId) {
    return employeesTaSettingsMapper.covertToEmployeesTaSettingsDto(
        attendanceSettingsService.findEmployeesSettings(employeeId));
  }

  @GetMapping("time-and-attendance/static-timezones")
  public List<StaticTimezoneDto> findAllTimeZones() {
    final List<StaticTimezone> staticTimezones = attendanceSettingsService.findAllStaticTimeZones();
    return ReflectionUtil.convertTo(staticTimezones, StaticTimezoneDto.class);
  }
}
