package shamu.company.company.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.company.dto.CompanyBenefitsSettingDto;
import shamu.company.company.dto.OfficeCreateDto;
import shamu.company.company.dto.OfficeDto;
import shamu.company.company.dto.OfficeSizeDto;
import shamu.company.company.entity.Company;
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.mapper.OfficeMapper;
import shamu.company.company.service.CompanyService;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.employee.dto.SelectFieldSizeDto;
import shamu.company.employee.service.EmployeeService;
import shamu.company.job.entity.Job;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;

@RestApiController
public class CompanyRestController extends BaseRestController {

  private final CompanyService companyService;

  private final EmployeeService employeeService;

  private final OfficeMapper officeMapper;

  @Autowired
  public CompanyRestController(
      final CompanyService companyService,
      final EmployeeService employeeService,
      final OfficeMapper officeMapper) {
    this.companyService = companyService;
    this.employeeService = employeeService;
    this.officeMapper = officeMapper;
  }

  @GetMapping("departments")
  public List<SelectFieldSizeDto> findDepartments() {
    return companyService.findDepartmentsByCompanyId(findCompanyId());
  }

  @PostMapping("departments")
  @PreAuthorize("hasAuthority('CREATE_DEPARTMENT')")
  public Department createDepartment(@RequestBody final String name) {
    return companyService.saveDepartmentsByCompany(name, findCompanyId());
  }

  @PostMapping("departments/{id}/jobs")
  @PreAuthorize("hasPermission(#id,'DEPARTMENT','CREATE_JOB')")
  public SelectFieldInformationDto saveJobsByDepartment(
      @PathVariable final String id, @RequestBody final String name) {
    final Job job = companyService.saveJobsByDepartmentId(id, name);
    return new SelectFieldInformationDto(job.getId(), job.getTitle());
  }

  @GetMapping("offices")
  @PreAuthorize("hasAuthority('VIEW_USER_JOB')")
  public List<OfficeSizeDto> findOffices() {
    return companyService.findOfficesByCompany(findCompanyId());
  }

  @PostMapping("offices")
  @PreAuthorize("hasAuthority('CREATE_OFFICE')")
  public OfficeDto saveOffice(@RequestBody final OfficeCreateDto officeCreateDto) {
    final Office office = officeCreateDto.getOffice();
    office.setCompany(new Company(findCompanyId()));
    return officeMapper.convertToOfficeDto(companyService.saveOffice(office));
  }

  @GetMapping("user-options")
  @PreAuthorize("hasAuthority('VIEW_USER_JOB')")
  public List<SelectFieldInformationDto> findUsers() {
    final List<User> users = employeeService.findByCompanyId(findCompanyId());
    return collectUserPersonInformations(users);
  }

  @GetMapping("departments/manager-candidate/{userId}/{departmentId}/users")
  @PreAuthorize(
      "hasPermission(#departmentId,'DEPARTMENT','VIEW_JOB')"
          + "and hasPermission(#userId,'USER','VIEW_JOB')")
  public List<SelectFieldInformationDto> findUsersFromDepartment(
      @PathVariable final String userId, @PathVariable final String departmentId) {
    final List<User> users;

    users = employeeService.findByCompanyId(findCompanyId());

    final List<SelectFieldInformationDto> selectFieldInformationDtos =
        collectUserPersonInformations(users);

    selectFieldInformationDtos.sort(Comparator.comparing(s -> s.getName().toLowerCase()));

    return selectFieldInformationDtos;
  }

  private List<SelectFieldInformationDto> collectUserPersonInformations(final List<User> users) {
    return users.stream()
        .map(
            user -> {
              UserPersonalInformation userInfo = user.getUserPersonalInformation();
              if (userInfo != null) {
                return new SelectFieldInformationDto(user.getId(), userInfo.getName());
              }
              return null;
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @GetMapping("benefits-setting")
  public CompanyBenefitsSettingDto findCompanyBenefitsSetting() {
    return companyService.findCompanyBenefitsSetting(findCompanyId());
  }

  @PatchMapping("benefits-setting/automatic-rollover")
  @PreAuthorize("hasAuthority('MANAGE_BENEFIT')")
  public HttpEntity updateBenefitSettingAutomaticRollover(@RequestBody final Boolean isTurnOn) {
    companyService.updateBenefitSettingAutomaticRollover(findCompanyId(), isTurnOn);
    return new ResponseEntity(HttpStatus.OK);
  }

  @PatchMapping("benefits-setting/enrollment-period")
  @PreAuthorize("hasAuthority('MANAGE_BENEFIT')")
  public HttpEntity updateEnrollmentPeriod(
      @RequestBody final CompanyBenefitsSettingDto companyBenefitsSettingDto) {
    companyService.updateEnrollmentPeriod(findCompanyId(), companyBenefitsSettingDto);
    return new ResponseEntity(HttpStatus.OK);
  }

  @PatchMapping("global-setting/company-name")
  @PreAuthorize("hasAuthority('UPDATE_COMPANY_NAME')")
  public String updateCompanyName(@RequestBody final String companyName) {
    final String companyId = findCompanyId();
    return companyService.updateCompanyName(companyName, companyId);
  }

  @GetMapping("paid-holidays-auto-enrolled")
  @PreAuthorize("hasAuthority('EDIT_PAID_HOLIDAY')")
  public boolean getIsPaidHolidayAutoEnrolled() {
    return companyService.findById(findCompanyId()).getIsPaidHolidaysAutoEnroll();
  }

  @PatchMapping("global-setting/paid-holidays-auto-enrolled")
  @PreAuthorize("hasAuthority('EDIT_PAID_HOLIDAY')")
  public HttpEntity<String> updateIsPaidHolidayAutoEnrolled(
      @RequestBody final boolean isAutoEnrolled) {
    companyService.updateIsPaidHolidaysAutoEnrolled(findCompanyId(), isAutoEnrolled);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
