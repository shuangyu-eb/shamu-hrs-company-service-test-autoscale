package shamu.company.job.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import shamu.company.employee.service.EmployeeService;
import shamu.company.job.dto.JobUpdateDto;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.repository.JobUserRepository;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserCompensation;
import shamu.company.user.entity.mapper.UserCompensationMapper;
import shamu.company.user.service.UserService;
import shamu.company.utils.Auth0Util;

@Service
public class JobUserService {

  private final JobUserRepository jobUserRepository;

  private final UserService userService;

  private final JobUserMapper jobUserMapper;

  private final UserCompensationMapper userCompensationMapper;

  private final EmployeeService employeeService;

  private final Auth0Util auth0Util;


  public JobUserService(final JobUserRepository jobUserRepository,
      final UserService userService,
      final JobUserMapper jobUserMapper,
      final UserCompensationMapper userCompensationMapper,
      final EmployeeService employeeService,
      final Auth0Util auth0Util) {
    this.jobUserRepository = jobUserRepository;
    this.userService = userService;
    this.userCompensationMapper = userCompensationMapper;
    this.jobUserMapper = jobUserMapper;
    this.employeeService = employeeService;
    this.auth0Util = auth0Util;

  }

  public JobUser getJobUserByUserId(final Long userId) {
    return jobUserRepository.findByUserId(userId);
  }

  public void updateJobInfo(final Long id, final JobUpdateDto jobUpdateDto, final Long companyId) {
    final User user = userService.findUserById(id);
    JobUser jobUser = jobUserRepository.findJobUserByUser(user);
    List<User> users = new ArrayList<>();
    if (jobUser == null) {
      jobUser = new JobUser();
      jobUser.setUser(user);
    } else {
      users = employeeService
          .findDirectReportsEmployersAndEmployeesByCompanyId(
              companyId,
              user.getId());
    }
    jobUserMapper.updateFromJobUpdateDto(jobUser, jobUpdateDto);
    jobUserRepository.save(jobUser);

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
