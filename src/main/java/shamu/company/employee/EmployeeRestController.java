package shamu.company.employee;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.job.JobUserDto;
import shamu.company.user.UserService;

@RestApiController
public class EmployeeRestController {

  @Autowired
  UserService userService;

  @GetMapping("employees")
  public List<JobUserDto> getAllEmployees() {
    return userService.findAllEmployees();
  }
}
