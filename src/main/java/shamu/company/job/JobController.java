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
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.service.JobUserService;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.service.UserService;

@RestApiController
public class JobController extends BaseRestController {

  private final JobUserService jobUserService;

  private final UserService userService;

  private final JobUserMapper jobUserMapper;

  private final UserCompensationMapper userCompensationMapper;

  @Autowired
  public JobController(final JobUserService jobUserService,
      final UserService userService,
      final JobUserMapper jobUserMapper,
      final UserCompensationMapper userCompensationMapper) {
    this.jobUserService = jobUserService;
    this.userService = userService;
    this.jobUserMapper = jobUserMapper;
    this.userCompensationMapper = userCompensationMapper;
  }


  @PatchMapping("users/{id}/jobs")
  @PreAuthorize("hasPermission(#id,'USER', 'EDIT_USER')")
  public HttpEntity updateJobInfo(@PathVariable @HashidsFormat final Long id,
      @RequestBody final JobUpdateDto jobUpdateDto) {
    final User user = userService.findUserById(id);
    JobUser jobUser = jobUserService.getJobUserByUserId(id);
    if (jobUser == null) {
      jobUser = new JobUser();
      jobUser.setUser(user);
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
    if (managerId != null && !user.getId().equals(managerId)) {
      final User manager = userService.findUserById(managerId);
      user.setManagerUser(manager);
      if (manager.getRole() == Role.NON_MANAGER) {
        userService.saveUserWithRole(user, Role.MANAGER);
      }
    }
    userService.save(user);

    return new ResponseEntity(HttpStatus.OK);
  }
}
