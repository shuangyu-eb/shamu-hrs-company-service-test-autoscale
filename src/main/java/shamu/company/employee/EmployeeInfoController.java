package shamu.company.employee;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.employee.dto.EmployeeRelatedInformationDto;
import shamu.company.job.JobUserDto;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

@RestApiController
public class EmployeeInfoController {

  @Autowired
  UserService userService;

  @GetMapping("/employees/{id}/info")
  public EmployeeRelatedInformationDto getEmployeeInfoByUserId(@PathVariable Long id) {

    User employee = userService.findEmployeeInfoByUserId(id);

    JobUserDto managerjobUserDto = userService
        .findEmployeeInfoByEmployeeId(employee.getManagerUser().getId());
    JobUserDto jobUserDto = userService.findEmployeeInfoByEmployeeId(id);

    List<JobUserDto> reports = userService.findDirectReportsByManagerId(id);
    EmployeeRelatedInformationDto employeeRelatedInformationDto = new EmployeeRelatedInformationDto(
        id, jobUserDto, managerjobUserDto, reports);

    return employeeRelatedInformationDto;
  }
}
