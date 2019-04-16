package shamu.company.employee.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.employee.dto.GeneralObjectDto;
import shamu.company.employee.pojo.OfficePojo;
import shamu.company.employee.service.EmployeeService;
import shamu.company.job.JobUserDto;
import shamu.company.user.UserService;

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

  @GetMapping(value = {"initial-form-information"})
  @ResponseBody
  public List<GeneralObjectDto> getJobInformation() {
    return employeeService.getJobInformation();
  }

  @PostMapping(value = "add-employmentType")
  @ResponseBody
  public Long saveEmploymentType(String employmentType) {
    return employeeService.saveEmploymentType(employmentType);
  }

  @PostMapping(value = "add-department")
  @ResponseBody
  public Long saveDepartment(String department) {
    return employeeService.saveDepartment(department);
  }

  @PostMapping(value = "add-officeLocation")
  @ResponseBody
  public Long saveOfficeLocation(@RequestBody OfficePojo officePojo) {
    return employeeService.saveOfficeLocation(officePojo);
  }
}
