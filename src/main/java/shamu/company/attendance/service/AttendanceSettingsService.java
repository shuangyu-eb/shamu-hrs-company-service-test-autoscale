package shamu.company.attendance.service;

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
import shamu.company.common.entity.PayrollDetail;
import shamu.company.common.entity.mapper.PayrollDetailMapper;
import shamu.company.common.service.PayrollDetailService;
import shamu.company.company.entity.Company;
import shamu.company.helpers.googlemaps.GoogleMapsHelper;
import shamu.company.job.entity.JobUser;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.user.entity.User;
import shamu.company.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

/** @author mshumaker */
@Service
public class AttendanceSettingsService {

  public static final String DEFAULT_TIME_ZONE = "America/Chicago";

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

  private final JobUserRepository jobUserRepository;

  private final GoogleMapsHelper googleMapsHelper;

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
      final PayrollDetailService payrollDetailService,
      final JobUserRepository jobUserRepository,
      final GoogleMapsHelper googleMapsHelper) {
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
    this.jobUserRepository = jobUserRepository;
    this.googleMapsHelper = googleMapsHelper;
  }

  public CompanyTaSetting findCompanySettings(final String companyId) {
    return companyTaSettingRepository.findByCompanyId(companyId);
  }

  public EmployeesTaSettingDto findEmployeesSettings(final String employeeId) {
    final Optional<User> user = userRepository.findById(employeeId);
    return employeesTaSettingsMapper.covertToEmployeesTaSettingsDto(
        employeesTaSettingRepository.findByEmployeeId(employeeId),
        user.map(User::getTimeZone).orElse(null));
  }

  public boolean existsByCompanyId(final String companyId) {
    return companyTaSettingRepository.existsByCompanyId(companyId);
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

  public void updateCompanySettings(
      final CompanyTaSettingsDto companyTaSettingsDto, final String companyId) {
    CompanyTaSetting companyTaSetting = companyTaSettingRepository.findByCompanyId(companyId);
    final PayrollDetail payrollDetail = payrollDetailService.findByCompanyId(companyId);
    final String payPeriodFrequencyId =
        payPeriodFrequencyRepository
            .findByName(companyTaSettingsDto.getPayFrequencyType().getId())
            .getId();
    if (companyTaSetting == null) {
      companyTaSetting = new CompanyTaSetting();
      companyTaSetting.setCompany(new Company(companyId));
    }
    companyTaSettingsMapper.updateFromCompanyTaSettingsDto(companyTaSetting, companyTaSettingsDto);
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

  public int findApprovalDaysBeforePayroll(final String companyId) {
    return companyTaSettingRepository.findApprovalDaysBeforePayroll(companyId);
  }

  public void initialTimezoneForOldDatas() {
    final List<User> lackTimezoneUsers = userRepository.findAllByTimeZoneIsNull();
    final StaticTimezone defaultTimezone = staticTimeZoneRepository.findByName(DEFAULT_TIME_ZONE);
    lackTimezoneUsers.forEach(
        user -> {
          final JobUser jobUser = jobUserRepository.findJobUserByUser(user);
          final CompanyTaSetting companyTaSetting =
              companyTaSettingRepository.findByCompanyId(user.getCompany().getId());
          String timezoneName = "";
          if (jobUser != null
              && jobUser.getOffice() != null
              && jobUser.getOffice().getOfficeAddress() != null) {
            timezoneName =
                googleMapsHelper.findTimezoneByPostalCode(
                    jobUser.getOffice().getOfficeAddress().getPostalCode());
          }

          if (timezoneName !=null && !timezoneName.isEmpty()) {
            user.setTimeZone(staticTimeZoneRepository.findByName(timezoneName));
          } else if (companyTaSetting != null) {
            user.setTimeZone(companyTaSetting.getTimeZone());
          } else {
            user.setTimeZone(defaultTimezone);
          }
          userRepository.save(user);
        });
  }
}
