package shamu.company.company;

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
import shamu.company.company.entity.Department;
import shamu.company.company.entity.Office;
import shamu.company.company.entity.mapper.OfficeMapper;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.employee.entity.EmploymentType;
import shamu.company.employee.service.EmployeeService;
import shamu.company.hashids.HashidsFormat;
import shamu.company.job.entity.Job;
import shamu.company.user.entity.User;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.utils.UserNameUtil;

@RestApiController
public class CompanyRestController extends BaseRestController {

  private final CompanyService companyService;

  private final EmployeeService employeeService;

  private final OfficeMapper officeMapper;

  @Autowired
  public CompanyRestController(final CompanyService companyService,
      final EmployeeService employeeService,
      final OfficeMapper officeMapper) {
    this.companyService = companyService;
    this.employeeService = employeeService;
    this.officeMapper = officeMapper;
  }


  @GetMapping("departments")
  public List<SelectFieldInformationDto> getDepartments() {
    return companyService.getDepartmentsByCompany(getCompany())
        .stream()
        .map(SelectFieldInformationDto::new)
        .collect(Collectors.toList());
  }


  @PostMapping("departments")
  @PreAuthorize("hasAuthority('CREATE_DEPARTMENT')")
  public Department createDepartment(@RequestBody final String name) {
    return companyService.saveDepartmentsByCompany(name, getCompany());
  }

  @GetMapping("departments/{id}/jobs")
  @PreAuthorize("hasPermission(#id,'DEPARTMENT','VIEW_JOB')")
  public List<SelectFieldInformationDto> getJobsByDepartment(
      @PathVariable @HashidsFormat final Long id) {
    return companyService.getJobsByDepartmentId(id).stream()
        .map(job -> new SelectFieldInformationDto(job.getId(), job.getTitle()))
        .collect(Collectors.toList());
  }


  @PostMapping("departments/{id}/jobs")
  @PreAuthorize("hasPermission(#id,'DEPARTMENT','CREATE_JOB')")
  public SelectFieldInformationDto saveJobsByDepartment(@PathVariable @HashidsFormat final Long id,
      @RequestBody final String name) {
    final Job job = companyService.saveJobsByDepartmentId(id, name);
    return new SelectFieldInformationDto(job.getId(), job.getTitle());
  }

  @GetMapping("employment-types")
  public List<SelectFieldInformationDto> getEmploymentTypes() {
    return companyService.getEmploymentTypesByCompany(getCompany())
        .stream()
        .map(SelectFieldInformationDto::new)
        .collect(Collectors.toList());
  }


  @PostMapping(value = "employment-types")
  @PreAuthorize("hasAuthority('CREATE_EMPLOYEE_TYPE')")
  public SelectFieldInformationDto createEmploymentType(@RequestBody final String name) {
    final EmploymentType employmentType = companyService.saveEmploymentType(name, getCompany());
    return new SelectFieldInformationDto(employmentType.getId(), employmentType.getName());
  }


  @GetMapping("offices")
  @PreAuthorize("hasAuthority('VIEW_USER_JOB')")
  public List<OfficeDto> getOffices() {
    return officeMapper.convertToOfficeDto(companyService.getOfficesByCompany(getCompany()));
  }

  @PostMapping("offices")
  @PreAuthorize("hasAuthority('CREATE_OFFICE')")
  public OfficeDto saveOffice(@RequestBody final OfficeCreateDto officeCreateDto) {
    final Office office = officeCreateDto.getOffice();
    office.setCompany(getCompany());
    return officeMapper.convertToOfficeDto(companyService.saveOffice(office));
  }

  @GetMapping("departments/{id}/users")
  @PreAuthorize("hasPermission(#id,'DEPARTMENT','VIEW_USER_JOB')")
  public List<SelectFieldInformationDto> getUsers(@PathVariable @HashidsFormat final Long id) {
    final List<User> users = employeeService
        .findEmployersAndEmployeesByDepartmentIdAndCompanyId(id, getCompany().getId());
    return users.stream().map(
        manager -> {
          UserPersonalInformation userInfo = manager.getUserPersonalInformation();
          String firstName = userInfo.getFirstName();
          String middleName = userInfo.getMiddleName();
          String lastName = userInfo.getLastName();
          return new SelectFieldInformationDto(manager.getId(),
              UserNameUtil.getUserName(firstName, middleName, lastName));
        })
        .collect(Collectors.toList());
  }
}
