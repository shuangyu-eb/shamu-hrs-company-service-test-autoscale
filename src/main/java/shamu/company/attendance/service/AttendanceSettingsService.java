package shamu.company.attendance.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import shamu.company.attendance.dto.CompanyTaSettingsDto;
import shamu.company.attendance.dto.EmployeesTaSettingDto;
import shamu.company.attendance.entity.CompanyTaSetting;
import shamu.company.attendance.entity.EmployeesTaSetting;
import shamu.company.attendance.entity.StaticTimezone;
import shamu.company.attendance.entity.mapper.CompanyTaSettingsMapper;
import shamu.company.attendance.entity.mapper.EmployeesTaSettingsMapper;
import shamu.company.attendance.repository.CompanyTaSettingRepository;
import shamu.company.attendance.repository.EmployeesTaSettingRepository;
import shamu.company.attendance.repository.PayPeriodFrequencyRepository;
import shamu.company.attendance.repository.StaticTimeZoneRepository;
import shamu.company.user.entity.User;
import shamu.company.user.repository.UserRepository;

/** @author mshumaker */
@Service
public class AttendanceSettingsService {
  private final CompanyTaSettingRepository companyTaSettingRepository;

  private final EmployeesTaSettingRepository employeesTaSettingRepository;

  private final StaticTimeZoneRepository staticTimeZoneRepository;

  private final CompanyTaSettingsMapper companyTaSettingsMapper;

  private final PayPeriodFrequencyRepository payPeriodFrequencyRepository;

  private final EmployeesTaSettingsMapper employeesTaSettingsMapper;

  private final UserRepository userRepository;

  public AttendanceSettingsService(
      final CompanyTaSettingRepository companyTaSettingRepository,
      final EmployeesTaSettingRepository employeesTaSettingRepository,
      final StaticTimeZoneRepository staticTimeZoneRepository,
      final CompanyTaSettingsMapper companyTaSettingsMapper,
      final PayPeriodFrequencyRepository payPeriodFrequencyRepository,
      final EmployeesTaSettingsMapper employeesTaSettingsMapper,
      final UserRepository userRepository) {
    this.companyTaSettingRepository = companyTaSettingRepository;
    this.employeesTaSettingRepository = employeesTaSettingRepository;
    this.staticTimeZoneRepository = staticTimeZoneRepository;
    this.companyTaSettingsMapper = companyTaSettingsMapper;
    this.payPeriodFrequencyRepository = payPeriodFrequencyRepository;
    this.employeesTaSettingsMapper = employeesTaSettingsMapper;
    this.userRepository = userRepository;
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

  public void updateCompanySettings(
      final CompanyTaSettingsDto companyTaSettingsDto, final String companyId) {
    final CompanyTaSetting companyTaSetting = companyTaSettingRepository.findByCompanyId(companyId);
    final String payPeriodFrequencyId =
        payPeriodFrequencyRepository
            .findByName(companyTaSettingsDto.getPayFrequencyType().getId())
            .getId();
    companyTaSettingsMapper.updateFromCompanyTaSettingsDto(
        companyTaSetting, companyTaSettingsDto, payPeriodFrequencyId);
    companyTaSettingRepository.save(companyTaSetting);
  }

  public void updateEmployeeSettings(
      final String employeeId, final EmployeesTaSettingDto employeesTaSettingDto) {
    final Optional<User> employee = userRepository.findById(employeeId);
    EmployeesTaSetting employeesTaSetting =
        employeesTaSettingRepository.findByEmployeeId(employeeId);
    if (employeesTaSetting == null) {
      employeesTaSetting = new EmployeesTaSetting();
    }
    employeesTaSettingDto.setEmployee(employee.orElseGet(User::new));
    employeesTaSettingsMapper.updateFromEmployeeTaSettingsDto(
        employeesTaSetting, employeesTaSettingDto);
    employeesTaSettingRepository.save(employeesTaSetting);
  }
}
