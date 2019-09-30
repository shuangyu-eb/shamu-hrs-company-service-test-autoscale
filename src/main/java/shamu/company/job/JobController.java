package shamu.company.job;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.employee.service.EmployeeService;
import shamu.company.hashids.HashidsFormat;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.service.JobUserService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.service.UserService;
import shamu.company.utils.Auth0Util;

@RestApiController
public class JobController extends BaseRestController {

  private final JobUserService jobUserService;

  private final UserService userService;

  private final JobUserMapper jobUserMapper;

  private final UserCompensationMapper userCompensationMapper;

  private final EmployeeService employeeService;

  private final Auth0Util auth0Util;

  @Autowired
  public JobController(final JobUserService jobUserService,
      final UserService userService,
      final JobUserMapper jobUserMapper,
      final UserCompensationMapper userCompensationMapper,
      final EmployeeService employeeService,
      final Auth0Util auth0Util) {
    this.jobUserService = jobUserService;
    this.userService = userService;
    this.jobUserMapper = jobUserMapper;
    this.userCompensationMapper = userCompensationMapper;
    this.employeeService = employeeService;
    this.auth0Util = auth0Util;
  }


  @PatchMapping("users/{id}/jobs")
  @PreAuthorize("hasPermission(#id,'USER', 'EDIT_USER')")
  public HttpEntity updateJobInfo(@PathVariable @HashidsFormat final Long id,
      @RequestBody final JobUpdateDto jobUpdateDto) {
    final User user = userService.findUserById(id);
    JobUser jobUser = jobUserService.getJobUserByUser(user);
    List<User> users = new ArrayList<>();
    if (jobUser == null) {
      jobUser = new JobUser();
      jobUser.setUser(user);
    } else {
      users = employeeService
          .findDirectReportsEmployersAndEmployeesByCompanyId(
              getCompanyId(),
              user.getId());
    }
    jobUserMapper.updateFromJobUpdateDto(jobUser, jobUpdateDto);
    jobUserService.save(jobUser);

    UserCompensation userCompensation = user.getUserCompensation();
    if (null == userCompensation) {
      userCompensation = new UserCompensation();
    }
    if (jobUpdateDto.getCompensationWage() != null
        && jobUpdateDto.getCompensationFrequencyId() != null) {
      userCompensationMapper.updateFromJobUpdateDto(userCompensation, jobUpdateDto);
      userCompensation.setUserId(user.getId());
      userCompensation = userService.saveUserCompensation(userCompensation);
    }

    user.setUserCompensation(userCompensation);

    final Long managerId = jobUpdateDto.getManagerId();
    if (managerId != null && (user.getManagerUser() == null
            || !user.getManagerUser().getId().equals(managerId))) {
      final User manager = userService.findUserById(managerId);
      final Role role = auth0Util.getUserRole(manager.getUserId());
      if (Role.EMPLOYEE == role) {
        auth0Util.updateRoleWithUserId(manager.getUserId(), Role.MANAGER.name());
      }
      if (userService.findUserById(id).getManagerUser() == null) {
        manager.setManagerUser(null);

      } else if (isSubordinate(id, managerId)) {
        manager.setManagerUser(user.getManagerUser());
      }
      user.setManagerUser(manager);
      userService.save(manager);
      final Role userRole = auth0Util.getUserRole(user.getUserId());
      if (userRole != Role.ADMIN) {
        users.removeIf(user1 -> user1.getId().equals(managerId));
        auth0Util.updateRoleWithUserId(
            user.getUserId(), users.isEmpty() ? Role.EMPLOYEE.name() : Role.MANAGER.name());
      }
    }
    userService.save(user);

    return new ResponseEntity(HttpStatus.OK);
  }

  private boolean isSubordinate(final Long userId, Long managerId) {
    User user = userService.findUserById(managerId);
    while (user.getManagerUser() != null && !user.getManagerUser().getId().equals(userId)) {
      managerId = user.getManagerUser().getId();
      user = userService.findUserById(managerId);
    }
    return user.getManagerUser() != null && user.getManagerUser().getId().equals(userId);
  }
}
