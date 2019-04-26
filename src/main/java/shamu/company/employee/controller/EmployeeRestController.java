package shamu.company.employee.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.employee.dto.EmployeeDto;
import shamu.company.employee.dto.EmployeeListSearchCondition;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.employee.pojo.OfficePojo;
import shamu.company.employee.service.EmployeeService;
import shamu.company.job.JobUserDto;
import shamu.company.user.service.UserService;

@RestApiController
public class EmployeeRestController extends BaseRestController {

  @Autowired EmployeeService employeeService;

  @Autowired
  UserService userService;

  @GetMapping("employees")
  public PageImpl<JobUserDto> getAllEmployees(
      EmployeeListSearchCondition employeeListSearchCondition) {
    return userService.getJobUserDtoList(employeeListSearchCondition, getCompany());
  }

  @GetMapping("employment-types")
  public List<SelectFieldInformationDto> getEmploymentTypes() {
    return employeeService.getEmploymentTypes();
  }

  @GetMapping("departments")
  public List<SelectFieldInformationDto> getDepartments() {
    return employeeService.getDepartments();
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
  public Long saveEmploymentType(String employmentType) {
    return employeeService.saveEmploymentType(employmentType).getId();
  }

  @PostMapping("department")
  public Long saveDepartment(String department) {
    return employeeService.saveDepartment(department).getId();
  }

  @PostMapping("office-location")
  public Long saveOfficeLocation(@RequestBody OfficePojo officePojo) {
    return employeeService.saveOfficeLocation(officePojo).getId();
  }

  @PostMapping("employees/welcome-email")
  public String getWelcomeEmail(@RequestBody String welcomeEmailPersonalMessage) {
    return userService.getWelcomeEmail(welcomeEmailPersonalMessage);
  }

  @PostMapping("employees")
  public HttpEntity addEmployee(@RequestBody EmployeeDto employee) {
    employeeService.addEmployee(employee, getUser());
    return new ResponseEntity(HttpStatus.OK);
  }
}
