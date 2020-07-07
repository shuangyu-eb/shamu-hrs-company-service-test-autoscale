package shamu.company.attendance.controller;

import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.attendance.dto.CompanyTaSettingsDto;
import shamu.company.attendance.entity.mapper.CompanyTaSettingsMapper;
import shamu.company.attendance.service.AttendanceSettingsService;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;

@RestApiController
public class AttendanceSettingsController extends BaseRestController {

  final AttendanceSettingsService attendanceSettingsService;

  final CompanyTaSettingsMapper companyTaSettingsMapper;

  public AttendanceSettingsController(
      final AttendanceSettingsService attendanceSettingsService,
      final CompanyTaSettingsMapper companyTaSettingsMapper) {
    this.attendanceSettingsService = attendanceSettingsService;
    this.companyTaSettingsMapper = companyTaSettingsMapper;
  }

  @GetMapping("time-and-attendance/settings")
  public CompanyTaSettingsDto findSettings() {
    return companyTaSettingsMapper.convertToCompanyTaSettingsDto(
        attendanceSettingsService.findCompanySettings(findCompanyId()));
  }
}
