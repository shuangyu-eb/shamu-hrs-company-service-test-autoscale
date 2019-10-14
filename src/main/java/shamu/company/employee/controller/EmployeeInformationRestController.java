package shamu.company.employee.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.employee.dto.EmployeeRelatedInformationDto;
import shamu.company.employee.service.EmployeeService;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.dto.BasicUserContactInformationDto;
import shamu.company.user.dto.BasicUserPersonalInformationDto;

@RestApiController
public class EmployeeInformationRestController extends BaseRestController {

  private final EmployeeService employeeService;

  public EmployeeInformationRestController(final EmployeeService employeeService) {
    this.employeeService = employeeService;
  }

  @GetMapping("/employees/{id}/info")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_USER_PERSONAL')")
  public EmployeeRelatedInformationDto getEmployeeInfoByUserId(
      @PathVariable @HashidsFormat final Long id) {
    return employeeService.getEmployeeInfoByUserId(id);
  }

  @GetMapping("users/{id}/personal")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_USER_PERSONAL')")
  public BasicUserPersonalInformationDto getPersonalMessage(
      @PathVariable @HashidsFormat final Long id) {
    return employeeService.getPersonalMessage(id, getAuthUser().getId());
  }

  @GetMapping("users/{id}/contact")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_USER_CONTACT')")
  public BasicUserContactInformationDto getContactMessage(
      @PathVariable @HashidsFormat final Long id) {
    return employeeService.getContactMessage(id, getAuthUser().getId());
  }

  @GetMapping("users/{id}/job")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_USER_JOB')")
  public BasicJobInformationDto getJobMessage(@PathVariable @HashidsFormat final Long id) {
    return employeeService.getJobMessage(id, getAuthUser().getId());
  }
}
