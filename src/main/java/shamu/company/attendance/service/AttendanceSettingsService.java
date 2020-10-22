package shamu.company.attendance.service;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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
import shamu.company.common.entity.PayrollDetail;
import shamu.company.common.entity.mapper.PayrollDetailMapper;
import shamu.company.common.service.PayrollDetailService;
import shamu.company.user.entity.User;
import shamu.company.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

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

  private final TimeSheetService timeSheetService;

  private final PayrollDetailMapper payrollDetailMapper;

  private final PayrollDetailService payrollDetailService;

  public AttendanceSettingsService(
      final CompanyTaSettingRepository companyTaSettingRepository,
      final EmployeesTaSettingRepository employeesTaSettingRepository,
      final StaticTimeZoneRepository staticTimeZoneRepository,
      final CompanyTaSettingsMapper companyTaSettingsMapper,
      final PayPeriodFrequencyRepository payPeriodFrequencyRepository,
      final EmployeesTaSettingsMapper employeesTaSettingsMapper,
      final UserRepository userRepository,
      final TimeSheetService timeSheetService,
      final PayrollDetailMapper payrollDetailMapper,
      final PayrollDetailService payrollDetailService) {
    this.companyTaSettingRepository = companyTaSettingRepository;
    this.employeesTaSettingRepository = employeesTaSettingRepository;
    this.staticTimeZoneRepository = staticTimeZoneRepository;
    this.companyTaSettingsMapper = companyTaSettingsMapper;
    this.payPeriodFrequencyRepository = payPeriodFrequencyRepository;
    this.employeesTaSettingsMapper = employeesTaSettingsMapper;
    this.userRepository = userRepository;
    this.timeSheetService = timeSheetService;
    this.payrollDetailMapper = payrollDetailMapper;
    this.payrollDetailService = payrollDetailService;
  }

  public CompanyTaSetting findCompanySetting() {
    final List<CompanyTaSetting> results = companyTaSettingRepository.findAll();
    if (CollectionUtils.isEmpty(results)) {
      return null;
    }
    return results.get(0);
  }

  public EmployeesTaSettingDto findEmployeesSettings(final String employeeId) {
    final Optional<User> user = userRepository.findById(employeeId);
    return employeesTaSettingsMapper.covertToEmployeesTaSettingsDto(
        employeesTaSettingRepository.findByEmployeeId(employeeId),
        user.map(User::getTimeZone).orElse(null));
  }

  public boolean exists() {
    return !CollectionUtils.isEmpty(companyTaSettingRepository.findAll());
  }

  public Boolean findEmployeeIsAttendanceSetUp(final String employeeId) {
    return timeSheetService.existByUser(employeeId);
  }

  public CompanyTaSetting saveCompanyTaSetting(final CompanyTaSetting companyTaSetting) {
    return companyTaSettingRepository.save(companyTaSetting);
  }

  public List<StaticTimezone> findAllStaticTimeZones() {
    return staticTimeZoneRepository.findAll();
  }

  public void updateCompanySettings(final CompanyTaSettingsDto companyTaSettingsDto) {
    final List<CompanyTaSetting> taSettings = companyTaSettingRepository.findAll();
    CompanyTaSetting companyTaSetting =
        CollectionUtils.isEmpty(taSettings) ? null : taSettings.get(0);
    final String payPeriodFrequencyId =
        payPeriodFrequencyRepository
            .findByName(companyTaSettingsDto.getPayFrequencyType().getId())
            .getId();
    if (companyTaSetting == null) {
      companyTaSetting = new CompanyTaSetting();
    }
    companyTaSettingsMapper.updateFromCompanyTaSettingsDto(companyTaSetting, companyTaSettingsDto);
    final PayrollDetail payrollDetail = payrollDetailService.find();
    payrollDetailMapper.updateFromCompanyTaSettingsDto(
        payrollDetail, companyTaSettingsDto, payPeriodFrequencyId);
    companyTaSettingRepository.save(companyTaSetting);
    payrollDetailService.savePayrollDetail(payrollDetail);
  }

  public void updateEmployeeSettings(
      final String employeeId, final EmployeesTaSettingDto employeesTaSettingDto) {
    final Optional<User> employee = userRepository.findById(employeeId);
    final EmployeesTaSetting employeesTaSetting =
        employeesTaSettingRepository.findByEmployeeId(employeeId);
    if (employeesTaSettingDto.getTimeZone() != null && employee.isPresent()) {
      final User user = employee.get();
      user.setTimeZone(employeesTaSettingDto.getTimeZone());
      userRepository.save(user);
    }
    if (employeesTaSetting != null) {
      employeesTaSettingDto.setEmployee(employee.orElseGet(User::new));
      employeesTaSettingsMapper.updateFromEmployeeTaSettingsDto(
          employeesTaSetting, employeesTaSettingDto);
      employeesTaSettingRepository.save(employeesTaSetting);
    }
  }

  public int findApprovalDaysBeforePayroll() {
    return companyTaSettingRepository.findApprovalDaysBeforePayroll();
  }

  public Integer findOvertimeAlertMinutes() {
    final CompanyTaSetting companyTaSetting = findCompanySetting();
    if (companyTaSetting != null) {
      return findCompanySetting().getOvertimeAlert();
    }
    return null;
  }
}
