package shamu.company.attendance.service;

import java.util.List;
import org.springframework.stereotype.Service;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeesTaSetting;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.attendance.repository.CompanyTaSettingRepository;
import shamu.company.attendance.repository.EmployeesTaSettingRepository;
import shamu.company.attendance.repository.StaticTimeZoneRepository;

/** @author mshumaker */
@Service
public class AttendanceSettingsService {
  private final CompanyTaSettingRepository companyTaSettingRepository;

  private final EmployeesTaSettingRepository employeesTaSettingRepository;

  private final StaticTimeZoneRepository staticTimeZoneRepository;

  public AttendanceSettingsService(final CompanyTaSettingRepository companyTaSettingRepository,
      final EmployeesTaSettingRepository employeesTaSettingRepository,
      final StaticTimeZoneRepository staticTimeZoneRepository) {
    this.companyTaSettingRepository = companyTaSettingRepository;
    this.employeesTaSettingRepository = employeesTaSettingRepository;
    this.staticTimeZoneRepository = staticTimeZoneRepository;
  }

  public CompanyTaSetting findCompanySettings(final String companyId) {
    return companyTaSettingRepository.findByCompanyId(companyId);
  }

  public EmployeesTaSetting findEmployeesSettings(final String employeeId) {
    return employeesTaSettingRepository.findByEmployeeId(employeeId);
  }

  public boolean existsByCompanyId(final String companyId) {
    return companyTaSettingRepository.existsByCompanyId(companyId);
  }

  public CompanyTaSetting saveCompanyTaSetting(final CompanyTaSetting companyTaSetting) {
    return companyTaSettingRepository.save(companyTaSetting);
  }

  public List<StaticTimezone> findAllStaticTimeZones() {
    return staticTimeZoneRepository.findAll();
  }
}
