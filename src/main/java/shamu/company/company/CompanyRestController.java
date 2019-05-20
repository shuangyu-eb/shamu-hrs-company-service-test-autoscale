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

  @Autowired
  public CompanyRestController(CompanyService companyService,
      EmployeeService employeeService) {
    this.companyService = companyService;
    this.employeeService = employeeService;
  }


  @GetMapping("departments")
  public List<SelectFieldInformationDto> getDepartments() {
    return companyService.getDepartmentsByCompany(this.getCompany())
        .stream()
        .map(SelectFieldInformationDto::new)
        .collect(Collectors.toList());
  }


  @PostMapping("departments")
  public Department createDepartment(@RequestBody String name) {
    return companyService.saveDepartmentsByCompany(name, this.getCompany());
  }

  @GetMapping("departments/{id}/jobs")
  public List<SelectFieldInformationDto> getJobsByDepartment(@PathVariable @HashidsFormat Long id) {
    return companyService.getJobsByDepartmentId(id).stream()
        .map(job -> new SelectFieldInformationDto(job.getId(), job.getTitle()))
        .collect(Collectors.toList());
  }


  @PostMapping("departments/{id}/jobs")
  public SelectFieldInformationDto saveJobsByDepartment(@PathVariable @HashidsFormat Long id,
      @RequestBody String name) {
    Job job = companyService.saveJobsByDepartmentId(id, name);
    return new SelectFieldInformationDto(job.getId(), job.getTitle());
  }

  @GetMapping("employment-types")
  // TODO permission
  public List<SelectFieldInformationDto> getEmploymentTypes() {
    return companyService.getEmploymentTypesByCompany(this.getCompany())
        .stream()
        .map(SelectFieldInformationDto::new)
        .collect(Collectors.toList());
  }


  @PostMapping(value = "employment-types")
  // TODO permission
  public SelectFieldInformationDto createEmploymentType(@RequestBody String name) {
    EmploymentType employmentType = companyService.saveEmploymentType(name, this.getCompany());
    return new SelectFieldInformationDto(employmentType.getId(), employmentType.getName());
  }


  @GetMapping("offices")
  @PreAuthorize("hasAuthority('VIEW_USER_JOB')")
  // TODO refactor, Permission
  public List<OfficeDto> getOffices() {
    return companyService.getOfficesByCompany(this.getCompany()).stream()
        .map(OfficeDto::new).collect(Collectors.toList());
  }

  @PostMapping("offices")
  // TODO refactor, Permission
  public OfficeDto saveOffice(@RequestBody OfficeCreateDto officeCreateDto) {
    Office office = officeCreateDto.getOffice();
    office.setCompany(this.getCompany());
    return new OfficeDto(companyService.saveOffice(office));
  }

  @GetMapping("departments/{id}/users")
  public List<SelectFieldInformationDto> getUsers(@PathVariable @HashidsFormat Long id) {
    List<User> users = employeeService
        .findEmployersAndEmployeesByDepartmentIdAndCompanyId(id, this.getCompany().getId());
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
