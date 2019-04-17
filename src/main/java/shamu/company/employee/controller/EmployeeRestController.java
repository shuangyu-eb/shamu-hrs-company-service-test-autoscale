package shamu.company.employee.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.employee.dto.SelectFieldInformationDto;
import shamu.company.employee.pojo.OfficePojo;
import shamu.company.employee.service.EmployeeService;
import shamu.company.job.JobUserDto;
import shamu.company.user.service.UserService;

@RestApiController
public class EmployeeRestController {

  @Autowired
  EmployeeService employeeService;

  @Autowired
  UserService userService;

  @GetMapping("employees")
  public List<JobUserDto> getAllEmployees() {
    return userService.findAllEmployees();
  }

  @GetMapping(value = {"employment-types"})
  @ResponseBody
  public List<SelectFieldInformationDto> getEmploymentTypes() {
    return employeeService.getEmploymentTypes();
  }

  @GetMapping(value = {"departments"})
  @ResponseBody
  public List<SelectFieldInformationDto> getDepartments() {
    return employeeService.getDepartments();
  }

  @GetMapping(value = {"office-locations"})
  @ResponseBody
  public List<SelectFieldInformationDto> getOfficeLocations() {
    return employeeService.getOfficeLocations();
  }

  @GetMapping(value = {"managers"})
  @ResponseBody
  public List<SelectFieldInformationDto> getManagers() {
    return employeeService.getManagers();
  }

  @PostMapping(value = "employment-type")
  @ResponseBody
  public Long saveEmploymentType(String employmentType) {
    return employeeService.saveEmploymentType(employmentType);
  }

  @PostMapping(value = "department")
  @ResponseBody
  public Long saveDepartment(String department) {
    return employeeService.saveDepartment(department);
  }

  @PostMapping(value = "office-location")
  @ResponseBody
  public Long saveOfficeLocation(@RequestBody OfficePojo officePojo) {
    return employeeService.saveOfficeLocation(officePojo);
  }
}
