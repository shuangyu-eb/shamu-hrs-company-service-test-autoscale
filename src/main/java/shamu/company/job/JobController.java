package shamu.company.job;

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
import shamu.company.hashids.HashidsFormat;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.job.entity.JobUser;
import shamu.company.job.service.JobUserService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.service.UserService;

@RestApiController
public class JobController extends BaseRestController {

  private final JobUserService jobUserService;

  private final UserService userService;

  @Autowired
  public JobController(JobUserService jobUserService, UserService userService) {
    this.jobUserService = jobUserService;
    this.userService = userService;
  }


  @PatchMapping("users/{id}/jobs")
  @PreAuthorize("hasPermission(#id,'USER', 'EDIT_USER')")
  public HttpEntity updateJobInfo(@PathVariable @HashidsFormat Long id,
      @RequestBody JobUpdateDto jobUpdateDto) {
    User user = userService.findUserById(id);
    JobUser jobUser = jobUserService.getJobUserByUserId(id);
    if (jobUser == null) {
      jobUser = new JobUser();
      jobUser.setUser(user);
    }
    jobUser = jobUpdateDto.updateJobUser(jobUser);
    jobUserService.save(jobUser);

    UserCompensation userCompensation = jobUpdateDto
        .updateUserCompensation(user.getUserCompensation());
    userCompensation.setUserId(user.getId());
    userCompensation = userService.saveUserCompensation(userCompensation);

    user.setUserCompensation(userCompensation);

    if (jobUpdateDto.getManagerId() != null) {
      User manager = userService.findUserById(jobUpdateDto.getManagerId());
      user.setManagerUser(manager);
      if (manager.getRole() == Role.NON_MANAGER) {
        userService.saveUserWithRole(user, Role.MANAGER);
      }
    }
    userService.save(user);

    return new ResponseEntity(HttpStatus.OK);
  }
}
