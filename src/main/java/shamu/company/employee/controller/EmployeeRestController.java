package shamu.company.employee.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.thymeleaf.context.Context;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.company.entity.Company;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.employee.pojo.OfficePojo;
import shamu.company.employee.service.EmployeeService;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.service.UserService;

@RestApiController
public class EmployeeRestController extends BaseRestController {

  private final EmployeeService employeeService;

  private final UserService userService;

  @Autowired
  public EmployeeRestController(EmployeeService employeeService, UserService userService) {
    this.employeeService = employeeService;
    this.userService = userService;
  }

  @GetMapping("employees")
  public Page<JobUserListItem> getAllEmployees(
      EmployeeListSearchCondition employeeListSearchCondition) {
    return userService.getAllEmployees(employeeListSearchCondition, this.getCompany());
  }

  @GetMapping("employees/my-team")
  @PreAuthorize("hasAuthority('VIEW_MY_TEAM')")
  public Page<JobUserListItem> getMyTeam(
      EmployeeListSearchCondition employeeListSearchCondition) {
    return userService.getMyTeam(employeeListSearchCondition, this.getUser());
  }

  @GetMapping("users")
  @PreAuthorize("hasAuthority('CREATE_USER')")
  public List<JobUserDto> getAllPolicyEmployees() {
    Company company = this.getUser().getCompany();
    return userService.findAllEmployees(company);
  }

  @GetMapping("office-locations")
  public List<SelectFieldInformationDto> getOfficeLocations() {
    return employeeService.getOfficeLocations();
  }

  @GetMapping("managers")
  public List<SelectFieldInformationDto> getManagers() {
    return employeeService.getManagers();
  }

  @PostMapping("employment-type")
  // TODO remove it, use the method in CompanyRestController
  public Long saveEmploymentType(String employmentType) {
    return employeeService.saveEmploymentType(employmentType).getId();
  }

  @PostMapping("department")
  // TODO  use the method in CompanyRestController
  public Long saveDepartment(String department) {
    return employeeService.saveDepartment(department).getId();
  }

  @PostMapping("office-location")
  public Long saveOfficeLocation(@RequestBody OfficePojo officePojo) {
    return employeeService.saveOfficeLocation(officePojo).getId();
  }

  @PostMapping("employees/welcome-email")
  public String getWelcomeEmail(@RequestBody String welcomeEmailPersonalMessage) {
    Context context = userService
        .getWelcomeEmailContext(welcomeEmailPersonalMessage, null);
    context.setVariable("createPasswordAddress", "#");
    return userService.getWelcomeEmail(context);
  }

  @PostMapping("employees")
  @PreAuthorize("hasAuthority('CREATE_USER')")
  public HttpEntity addEmployee(@RequestBody EmployeeDto employee) {
    employeeService.addEmployee(employee, getUser());
    return new ResponseEntity(HttpStatus.OK);
  }
}
