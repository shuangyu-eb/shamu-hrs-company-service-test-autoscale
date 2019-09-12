package shamu.company.employee.controller;

import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.Context;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.company.entity.Company;
import shamu.company.employee.dto.EmailResendDto;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.employee.dto.OrgChartDto;
import shamu.company.employee.service.EmployeeService;
import shamu.company.hashids.HashidsFormat;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

@RestApiController
public class EmployeeRestController extends BaseRestController {

  private final EmployeeService employeeService;

  private final UserService userService;

  @Autowired
  public EmployeeRestController(final EmployeeService employeeService,
      final UserService userService) {
    this.employeeService = employeeService;
    this.userService = userService;
  }

  @GetMapping("employees")
  public Page<JobUserListItem> getAllEmployees(
      final EmployeeListSearchCondition employeeListSearchCondition) {
    final Boolean isAdmin = getUser().getRole() == User.Role.ADMIN;
    return userService.getAllEmployees(employeeListSearchCondition, getCompany(), isAdmin);
  }

  @GetMapping("employees/my-team")
  @PreAuthorize("hasAuthority('VIEW_MY_TEAM')")
  public Page<JobUserListItem> getMyTeam(
      final EmployeeListSearchCondition employeeListSearchCondition) {
    return userService.getMyTeam(employeeListSearchCondition, getUser());
  }

  @GetMapping("users")
  @PreAuthorize("hasAuthority('CREATE_USER')")
  public List<JobUserDto> getAllPolicyEmployees() {
    final Company company = getUser().getCompany();
    return userService.findAllJobUsers(company);
  }

  @PostMapping("employees/welcome-email")
  public String getWelcomeEmail(
      @RequestBody(required = false) final String welcomeEmailPersonalMessage) {
    final Context context = userService.getWelcomeEmailContext(welcomeEmailPersonalMessage, null);
    context.setVariable("createPasswordAddress", "#");
    return userService.getWelcomeEmail(context);
  }

  @PostMapping("employees/welcome-email/resend")
  @PreAuthorize("hasAuthority('EDIT_USER')")
  public HttpEntity getWelcomeEmail(@RequestBody @Valid final EmailResendDto emailResend) {

    employeeService.resendEmail(emailResend);
    return new ResponseEntity(HttpStatus.OK);
  }

  @PostMapping("employees")
  @PreAuthorize("hasAuthority('CREATE_USER')")
  public HttpEntity addEmployee(@RequestBody final EmployeeDto employee) {
    employeeService.addEmployee(employee, getUser());
    return new ResponseEntity(HttpStatus.OK);
  }

  @PatchMapping("employees")
  @PreAuthorize("hasAuthority('EDIT_SELF')")
  public HttpEntity updateEmployee(@RequestBody final EmployeeDto employeeDto) {

    employeeService.updateEmployee(employeeDto, getUser());
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping("employees/org-chart")
  public OrgChartDto getOrgChart(
      @HashidsFormat @RequestParam(value = "userId", required = false) final Long userId) {
    return userService.getOrgChart(userId, getCompany());
  }
}
