package shamu.company.employee.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import shamu.company.authorization.Permission.Name;
import shamu.company.authorization.Type;
import shamu.company.authorization.annotation.HasPermission;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.employee.dto.BasicUserContactInformationDto;
import shamu.company.employee.dto.BasicUserPersonalInformationDto;
import shamu.company.employee.dto.EmployeeRelatedInformationDto;
import shamu.company.employee.dto.JobInformationDto;
import shamu.company.employee.dto.UserContactInformationDto;
import shamu.company.employee.dto.UserPersonalInformationDto;
import shamu.company.employee.dto.UserPersonalInformationForManagerDto;
import shamu.company.job.JobUserDto;
import shamu.company.job.entity.JobUser;
import shamu.company.job.service.JobUserService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.service.UserService;

@RestApiController
public class EmployeeInformationRestController extends BaseRestController {

  @Autowired
  UserService userService;

  @Autowired
  JobUserService jobUserService;

  @GetMapping("/employees/{id}/info")
  @HasPermission(targetId = "#id", targetType = Type.USER, permission = Name.VIEW_USER_PERSONAL)
  public EmployeeRelatedInformationDto getEmployeeInfoByUserId(@PathVariable Long id) {
    User employee = userService.findEmployeeInfoByUserId(id);

    JobUserDto managerjobUserDto = null;
    if (employee.getManagerUser() != null) {
      managerjobUserDto = userService
        .findEmployeeInfoByEmployeeId(employee.getManagerUser().getId());
    }
    JobUserDto jobUserDto = userService.findEmployeeInfoByEmployeeId(id);

    List<JobUserDto> reports = userService.findDirectReportsByManagerId(id);

    return new EmployeeRelatedInformationDto(id, jobUserDto, managerjobUserDto, reports);
  }

  @GetMapping("users/{id}/personal")
  @HasPermission(targetId = "#id", targetType = Type.USER, permission = Name.VIEW_USER_PERSONAL)
  public BasicUserPersonalInformationDto getPersonalMessage(@PathVariable Long id) {
    User user = this.getUser();
    User targetUser = userService.findUserById(id);
    UserPersonalInformation personalInformation = targetUser.getUserPersonalInformation();

    // The user's full personal message can only be accessed by admin and himself.
    if (user.getId().equals(id) || user.getRole() == Role.ADMIN) {
      return new UserPersonalInformationDto(personalInformation);
    }
    if (targetUser.getManagerUser().getId().equals(user.getId())) {
      return new UserPersonalInformationForManagerDto(personalInformation);
    }

    return new BasicUserPersonalInformationDto(personalInformation);
  }

  @GetMapping("users/{id}/contact")
  @HasPermission(targetId = "#id", targetType = Type.USER, permission = Name.VIEW_USER_CONTACT)
  public BasicUserContactInformationDto getContactMessage(@PathVariable Long id) {
    User user = this.getUser();
    User targetUser = userService.findUserById(id);
    UserContactInformation contactInformation = targetUser.getUserContactInformation();

    // The user's full contact message can only be accessed by admin, the manager and himself.
    if (user.getId().equals(id) || targetUser.getManagerUser().getId().equals(user.getId())
        || user.getRole() == Role.ADMIN) {
      return new UserContactInformationDto(contactInformation);
    }

    return new BasicUserContactInformationDto(contactInformation);
  }

  @GetMapping("users/{id}/job")
  @HasPermission(targetId = "#id", targetType = Type.USER, permission = Name.VIEW_USER_JOB)
  public BasicJobInformationDto getJobMessage(@PathVariable Long id) {
    User user = this.getUser();
    JobUser target = jobUserService.getJobUserByUserId(id);

    // The user's full job message can only be accessed by admin, the manager and himself.
    if (user.getId().equals(id) || target.getUser().getManagerUser().getId().equals(user.getId())
        || user.getRole() == Role.ADMIN) {
      return new JobInformationDto(target);
    }

    return new BasicJobInformationDto(target);
  }
}