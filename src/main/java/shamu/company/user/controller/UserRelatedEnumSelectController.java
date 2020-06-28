package shamu.company.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.benefit.entity.RetirementType;
import shamu.company.common.BaseRestController;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.service.EmploymentTypeService;
import shamu.company.job.entity.CompensationFrequency;
import shamu.company.user.entity.CompensationOvertimeStatus;
import shamu.company.user.entity.EmployeeType;
import shamu.company.user.entity.DeactivationReasons;
import shamu.company.user.entity.Ethnicity;
import shamu.company.user.entity.Gender;
import shamu.company.user.entity.MaritalStatus;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.service.CompensationFrequencyService;
import shamu.company.user.service.CompensationOvertimeStatusService;
import shamu.company.user.service.DeactivationReasonService;
import shamu.company.user.service.EmployeeTypesService;
import shamu.company.user.service.EthnicityService;
import shamu.company.user.service.GenderService;
import shamu.company.user.service.MaritalStatusService;
import shamu.company.user.service.RetirementTypeService;
import shamu.company.user.service.UserRoleService;
import shamu.company.user.service.UserStatusService;
import shamu.company.utils.ReflectionUtil;

import java.util.List;

@RestApiController
public class UserRelatedEnumSelectController extends BaseRestController {

  private final CompensationFrequencyService compensationFrequencyService;

  private final CompensationOvertimeStatusService compensationOvertimeStatusService;

  private final DeactivationReasonService deactivationReasonService;

  private final EthnicityService ethnicityService;

  private final GenderService genderService;

  private final MaritalStatusService maritalStatusService;

  private final RetirementTypeService retirementTypeService;

  private final UserRoleService userRoleService;

  private final UserStatusService userStatusService;

  private final EmployeeTypesService employeeTypesService;

  private final EmploymentTypeService employmentTypeService;

  public UserRelatedEnumSelectController(
      final CompensationFrequencyService compensationFrequencyService,
      final CompensationOvertimeStatusService compensationOvertimeStatusService,
      final DeactivationReasonService deactivationReasonService,
      final EthnicityService ethnicityService,
      final GenderService genderService,
      final MaritalStatusService maritalStatusService,
      final RetirementTypeService retirementTypeService,
      final UserRoleService userRoleService,
      final UserStatusService userStatusService,
      final EmployeeTypesService employeeTypesService,
      final EmploymentTypeService employmentTypeService) {
    this.compensationFrequencyService = compensationFrequencyService;
    this.compensationOvertimeStatusService = compensationOvertimeStatusService;
    this.deactivationReasonService = deactivationReasonService;
    this.ethnicityService = ethnicityService;
    this.genderService = genderService;
    this.maritalStatusService = maritalStatusService;
    this.retirementTypeService = retirementTypeService;
    this.userRoleService = userRoleService;
    this.userStatusService = userStatusService;
    this.employeeTypesService = employeeTypesService;
    this.employmentTypeService =employmentTypeService;
  }

  @GetMapping("compensation-frequencies")
  public List<CommonDictionaryDto> getCompensationFrequencies() {
    final List<CompensationFrequency> frequencies = compensationFrequencyService.findAll();
    return ReflectionUtil.convertTo(frequencies, CommonDictionaryDto.class);
  }

  @GetMapping("compensation-overtime-statuses")
  public List<CommonDictionaryDto> getCompensationStatuses() {
    final List<CompensationOvertimeStatus> overtimeStatuses =
        compensationOvertimeStatusService.findAll();
    return ReflectionUtil.convertTo(overtimeStatuses, CommonDictionaryDto.class);
  }

  @GetMapping("deactivation-reasons")
  public List<CommonDictionaryDto> getDeactivationReasons() {
    final List<DeactivationReasons> deactivationReasons = deactivationReasonService.findAll();
    return ReflectionUtil.convertTo(deactivationReasons, CommonDictionaryDto.class);
  }

  @GetMapping("ethnicities")
  public List<CommonDictionaryDto> getEthnicities() {
    final List<Ethnicity> ethnicities = ethnicityService.findAll();
    return ReflectionUtil.convertTo(ethnicities, CommonDictionaryDto.class);
  }

  @GetMapping("genders")
  public List<CommonDictionaryDto> getGenders() {
    final List<Gender> genders = genderService.findAll();
    return ReflectionUtil.convertTo(genders, CommonDictionaryDto.class);
  }

  @GetMapping("marital-statuses")
  public List<CommonDictionaryDto> getMaritalStatuses() {
    final List<MaritalStatus> maritalStatuses = maritalStatusService.findAll();
    return ReflectionUtil.convertTo(maritalStatuses, CommonDictionaryDto.class);
  }

  @GetMapping("retirement-types")
  public List<CommonDictionaryDto> getRetirementTypes() {
    final List<RetirementType> retirementTypes = retirementTypeService.findAll();
    return ReflectionUtil.convertTo(retirementTypes, CommonDictionaryDto.class);
  }

  @GetMapping("user-roles")
  public List<CommonDictionaryDto> getAllUserRoles() {
    final List<UserRole> userRoles = userRoleService.findAll();
    return ReflectionUtil.convertTo(userRoles, CommonDictionaryDto.class);
  }

  @GetMapping("user-statuses")
  public List<CommonDictionaryDto> getAllUserStatuses() {
    final List<UserStatus> userStatuses = userStatusService.findAll();
    return ReflectionUtil.convertTo(userStatuses, CommonDictionaryDto.class);
  }

  @GetMapping("employee-types")
  public List<CommonDictionaryDto> getAllEmployeeTypes() {
    final List<EmployeeType> employeeTypes = employeeTypesService.findAll();
    return ReflectionUtil.convertTo(employeeTypes, CommonDictionaryDto.class);
  }

  @GetMapping("employment-types")
  public List<CommonDictionaryDto> getAllEmploymentTypes() {
    final List<EmploymentType> employmentTypes = employmentTypeService.findAll();
    return ReflectionUtil.convertTo(employmentTypes, CommonDictionaryDto.class);
  }
}
