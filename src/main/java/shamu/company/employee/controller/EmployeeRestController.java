package shamu.company.employee.controller;

import java.util.List;
import javax.validation.Valid;
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
import shamu.company.common.exception.ForbiddenException;
import shamu.company.employee.dto.EmailResendDto;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.employee.dto.OrgChartDto;
import shamu.company.employee.service.EmployeeService;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.JobUserListItem;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

@RestApiController
public class EmployeeRestController extends BaseRestController {

  private final EmployeeService employeeService;

  private final UserService userService;

  public EmployeeRestController(final EmployeeService employeeService,
      final UserService userService) {
    this.employeeService = employeeService;
    this.userService = userService;
  }

  @GetMapping("employees")
  public Page<JobUserListItem> findAllEmployees(
      final EmployeeListSearchCondition employeeListSearchCondition) {
    return userService.findAllEmployees(findUserId(), employeeListSearchCondition);
  }

  @GetMapping("employees/my-team")
  @PreAuthorize("hasAuthority('VIEW_MY_TEAM')")
  public Page<JobUserListItem> findMyTeam(
      final EmployeeListSearchCondition employeeListSearchCondition) {
    return userService.getMyTeam(employeeListSearchCondition, findAuthUser().getId());
  }

  @GetMapping("employees/jobs-users")
  @PreAuthorize("hasAuthority('CREATE_USER')")
  public List<JobUserDto> findAllPolicyEmployees() {
    return userService.findAllJobUsers(findCompanyId());
  }

  @PostMapping("employees/welcome-email")
  @PreAuthorize("hasAuthority('CREATE_USER')")
  public String findWelcomeEmail(
      @RequestBody(required = false) final String welcomeEmailPersonalMessage) {

    final Context context = userService
        .findWelcomeEmailPreviewContext(findAuthUser().getId(), welcomeEmailPersonalMessage);
    return userService.getWelcomeEmail(context);
  }

  @PostMapping("employees/welcome-email/resend")
  @PreAuthorize("hasPermission(#emailResend.userId, 'USER', 'EDIT_USER')")
  public HttpEntity resendWelcomeEmail(@RequestBody @Valid final EmailResendDto emailResend) {

    employeeService.resendEmail(emailResend);
    return new ResponseEntity(HttpStatus.OK);
  }

  @PostMapping("employees")
  @PreAuthorize("hasAuthority('CREATE_USER')")
  public HttpEntity addEmployee(@RequestBody final EmployeeDto employee) {
    final User currentUser = userService.findById(findAuthUser().getId());
    employeeService.addEmployee(employee, currentUser);
    return new ResponseEntity(HttpStatus.OK);
  }

  @PatchMapping("employees/current")
  @PreAuthorize("@permissionUtils.isCurrentUserEmail(#employeeDto.emailWork)")
  public HttpEntity saveEmployeeSetUpInformation(@RequestBody final EmployeeDto employeeDto) {
    final User employee = userService.findById(findAuthUser().getId());
    if (employee.getVerifiedAt() != null) {
      throw new ForbiddenException(String.format("User with email %s already verified!",
          employeeDto.getEmailWork()));
    }

    employeeService.updateEmployee(employeeDto, employee);
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping("employees/org-chart")
  public List<OrgChartDto> findOrgChart(
      @RequestParam(value = "userId", required = false) final String userId) {
    return userService.getOrgChart(userId, findCompanyId());
  }
}
