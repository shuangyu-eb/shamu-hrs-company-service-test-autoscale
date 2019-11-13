package shamu.company.company.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
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
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.service.EmployeeService;
import shamu.company.hashids.HashidsFormat;
import shamu.company.job.entity.Job;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.UserService;
import shamu.company.utils.Auth0Util;
import shamu.company.utils.UserNameUtil;

@RestApiController
public class CompanyRestController extends BaseRestController {

  private final CompanyService companyService;

  private final EmployeeService employeeService;

  private final OfficeMapper officeMapper;

  private final UserService userService;

  private final Auth0Util auth0Util;

  @Autowired
  public CompanyRestController(final CompanyService companyService,
      final EmployeeService employeeService,
      final OfficeMapper officeMapper,
      final UserService userService,
      final Auth0Util auth0Util) {
    this.companyService = companyService;
    this.employeeService = employeeService;
    this.officeMapper = officeMapper;
    this.userService = userService;
    this.auth0Util = auth0Util;
  }


  @GetMapping("departments")
  public List<SelectFieldSizeDto> getDepartments() {
    return companyService.getDepartmentsByCompanyId(getCompanyId());
  }


  @PostMapping("departments")
  @PreAuthorize("hasAuthority('CREATE_DEPARTMENT')")
  public Department createDepartment(@RequestBody final String name) {
    return companyService.saveDepartmentsByCompany(name, getCompanyId());
  }

  @GetMapping("departments/{id}/jobs")
  @PreAuthorize("hasPermission(#id,'DEPARTMENT','VIEW_JOB')")
  public List<SelectFieldSizeDto> getJobsByDepartment(
      @PathVariable @HashidsFormat final Long id) {
    return companyService.getJobsByDepartmentId(id);
  }


  @PostMapping("departments/{id}/jobs")
  @PreAuthorize("hasPermission(#id,'DEPARTMENT','CREATE_JOB')")
  public SelectFieldInformationDto saveJobsByDepartment(@PathVariable @HashidsFormat final Long id,
      @RequestBody final String name) {
    final Job job = companyService.saveJobsByDepartmentId(id, name);
    return new SelectFieldInformationDto(job.getId(), job.getTitle());
  }

  @GetMapping("employment-types")
  public List<SelectFieldSizeDto> getEmploymentTypes() {
    return companyService.getEmploymentTypesByCompanyId(getCompanyId());
  }


  @PostMapping(value = "employment-types")
  @PreAuthorize("hasAuthority('CREATE_EMPLOYEE_TYPE')")
  public SelectFieldInformationDto createEmploymentType(@RequestBody final String name) {
    final EmploymentType employmentType = companyService.saveEmploymentType(name, getCompanyId());
    return new SelectFieldInformationDto(employmentType.getId(), employmentType.getName());
  }


  @GetMapping("offices")
  @PreAuthorize("hasAuthority('VIEW_USER_JOB')")
  public List<OfficeSizeDto> getOffices() {
    return companyService.getOfficesByCompany(getCompanyId());
  }

  @PostMapping("offices")
  @PreAuthorize("hasAuthority('CREATE_OFFICE')")
  public OfficeDto saveOffice(@RequestBody final OfficeCreateDto officeCreateDto) {
    final Office office = officeCreateDto.getOffice();
    office.setCompany(new Company(getCompanyId()));
    return officeMapper.convertToOfficeDto(companyService.saveOffice(office));
  }

  @GetMapping("departments/{id}/users")
  @PreAuthorize("hasPermission(#id,'DEPARTMENT','VIEW_USER_JOB')")
  public List<SelectFieldInformationDto> getUsers(@PathVariable @HashidsFormat final Long id) {
    final List<User> users = employeeService
        .findEmployersAndEmployeesByCompanyId(getCompanyId());
    return collectUserPersonInformations(users);
  }

  @GetMapping("departments/mayBeManager/{userId}/{departmentId}/users")
  @PreAuthorize("hasPermission(#departmentId,'DEPARTMENT','VIEW_JOB')"
          + "and hasPermission(#userId,'USER','VIEW_JOB')")
  public List<SelectFieldInformationDto> getUsersFromDepartment(
          @PathVariable @HashidsFormat final Long userId,
          @PathVariable @HashidsFormat final Long departmentId) {
    final List<User> users;
    final User user = userService.findUserById(userId);
    if (user.getManagerUser() == null) {
      users = employeeService.findDirectReportsEmployersAndEmployeesByCompanyId(
              getCompanyId(), user.getId());
    } else {
      users = employeeService.findEmployersAndEmployeesByCompanyId(getCompanyId());
    }

    final List<SelectFieldInformationDto> selectFieldInformationDtos =
            collectUserPersonInformations(users);

    selectFieldInformationDtos.sort((s1, s2) -> s1.getName().toLowerCase()
            .compareTo(s2.getName().toLowerCase()));

    return selectFieldInformationDtos;
  }

  private List<SelectFieldInformationDto> collectUserPersonInformations(final List<User> users) {
    return users.stream().map(
        user -> {
          UserPersonalInformation userInfo = user.getUserPersonalInformation();
          String firstName = userInfo.getFirstName();
          String middleName = userInfo.getMiddleName();
          String lastName = userInfo.getLastName();
          return new SelectFieldInformationDto(user.getId(),
              UserNameUtil.getUserName(firstName, middleName, lastName));
        }).collect(Collectors.toList());
  }
}
