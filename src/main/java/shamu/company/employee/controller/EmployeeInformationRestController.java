package shamu.company.employee.controller;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.email.Email;
import shamu.company.employee.dto.BasicJobInformationDto;
import shamu.company.employee.dto.EmployeeRelatedInformationDto;
import shamu.company.employee.dto.JobInformationDto;
import shamu.company.employee.service.EmployeeService;
import shamu.company.hashids.HashidsFormat;
import shamu.company.job.dto.JobUserDto;
import shamu.company.job.entity.JobUser;
import shamu.company.job.entity.mapper.JobUserMapper;
import shamu.company.job.service.JobUserService;
import shamu.company.user.dto.BasicUserContactInformationDto;
import shamu.company.user.dto.BasicUserPersonalInformationDto;
import shamu.company.user.entity.User;
import shamu.company.user.entity.User.Role;
import shamu.company.user.entity.UserContactInformation;
import shamu.company.user.entity.UserPersonalInformation;
import shamu.company.user.entity.UserStatus.Status;
import shamu.company.user.entity.mapper.UserContactInformationMapper;
import shamu.company.user.entity.mapper.UserMapper;
import shamu.company.user.entity.mapper.UserPersonalInformationMapper;
import shamu.company.user.service.UserService;
import shamu.company.utils.Auth0Util;

@RestApiController
public class EmployeeInformationRestController extends BaseRestController {

  private final UserService userService;

  private final EmployeeService employeeService;

  private final JobUserService jobUserService;

  private final JobUserMapper jobUserMapper;

  private final UserMapper userMapper;

  private final UserContactInformationMapper userContactInformationMapper;

  private final UserPersonalInformationMapper userPersonalInformationMapper;

  private final Auth0Util auth0Util;

  @Autowired
  public EmployeeInformationRestController(final UserService userService,
      final EmployeeService employeeService,
      final JobUserService jobUserService,
      final JobUserMapper jobUserMapper,
      final UserContactInformationMapper userContactInformationMapper,
      final UserPersonalInformationMapper userPersonalInformationMapper,
      final UserMapper userMapper,
      final Auth0Util auth0Util) {
    this.userService = userService;
    this.employeeService = employeeService;
    this.jobUserService = jobUserService;
    this.jobUserMapper = jobUserMapper;
    this.userContactInformationMapper = userContactInformationMapper;
    this.userPersonalInformationMapper = userPersonalInformationMapper;
    this.userMapper = userMapper;
    this.auth0Util = auth0Util;
  }

  @GetMapping("/employees/{id}/info")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_USER_PERSONAL')")
  public EmployeeRelatedInformationDto getEmployeeInfoByUserId(
      @PathVariable @HashidsFormat final Long id) {
    final User employee = userService.findUserById(id);
    final String emailAddress = employee.getUserContactInformation().getEmailWork();
    final Status userStatus = employee.getUserStatus().getStatus();

    Timestamp sendDate = null;
    if (userStatus == Status.PENDING_VERIFICATION) {

      final Email email = employeeService
          .getWelcomeEmail(emailAddress, employee.getCompany().getName());
      sendDate = email != null ? email.getSendDate() : null;
    }

    JobUserDto managerjobUserDto = null;
    if (employee.getManagerUser() != null) {
      managerjobUserDto =
          userService.findEmployeeInfoByEmployeeId(employee.getManagerUser().getId());
    }
    final JobUserDto jobUserDto = userService.findEmployeeInfoByEmployeeId(id);

    final List<JobUserDto> reports = userService.findDirectReportsByManagerId(id).stream()
        .map(user -> userService.findEmployeeInfoByEmployeeId(user.getId()))
        .collect(Collectors.toList());

    return jobUserMapper.convertToEmployeeRelatedInformationDto(id, emailAddress,
        userStatus.name(), sendDate, jobUserDto,
        managerjobUserDto, reports);
  }

  @GetMapping("users/{id}/personal")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_USER_PERSONAL')")
  public BasicUserPersonalInformationDto getPersonalMessage(
      @PathVariable @HashidsFormat final Long id) {
    final User targetUser = userService.findUserById(id);
    final UserPersonalInformation personalInformation = targetUser.getUserPersonalInformation();

    // The user's full personal message can only be accessed by admin and himself.
    final Role userRole = auth0Util.getUserRole(getUserId());
    if (getAuthUser().getId().equals(id) || userRole == Role.ADMIN) {
      return userPersonalInformationMapper
          .convertToEmployeePersonalInformationDto(personalInformation);
    }
    if (targetUser.getManagerUser().getId().equals(getAuthUser().getId())) {
      return userPersonalInformationMapper
          .convertToUserPersonalInformationForManagerDto(personalInformation);
    }

    return userPersonalInformationMapper
        .convertToBasicUserPersonalInformationDto(personalInformation);
  }

  @GetMapping("users/{id}/contact")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_USER_CONTACT')")
  public BasicUserContactInformationDto getContactMessage(
      @PathVariable @HashidsFormat final Long id) {
    final User targetUser = userService.findUserById(id);
    final UserContactInformation contactInformation = targetUser.getUserContactInformation();

    // The user's full contact message can only be accessed by admin, the manager and himself.
    final Role userRole = auth0Util
        .getUserRole(getUserId());
    if (getAuthUser().getId().equals(id)
        || targetUser.getManagerUser().getId().equals(getAuthUser().getId())
        || userRole == Role.ADMIN) {
      return userContactInformationMapper
          .convertToEmployeeContactInformationDto(contactInformation);
    }

    return userContactInformationMapper.convertToBasicUserContactInformationDto(contactInformation);
  }

  @GetMapping("users/{id}/job")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_USER_JOB')")
  public BasicJobInformationDto getJobMessage(@PathVariable @HashidsFormat final Long id) {
    final JobUser target = jobUserService.getJobUserByUserId(id);

    if (target == null) {
      final User targetUser = userService.findUserById(id);
      final Role targetUserRole = auth0Util
          .getUserRole(targetUser.getUserId());
      final BasicJobInformationDto resultUser =
          userMapper.convertToBasicJobInformationDto(targetUser);
      resultUser.setUserRole(targetUserRole);
      return resultUser;
    }

    final User targetUser = target.getUser();
    final Role targetUserRole = auth0Util
        .getUserRole(targetUser.getUserId());
    // The user's full job message can only be accessed by admin, the manager and himself.
    final Role userRole = auth0Util
        .getUserRole(getUserId());
    if (getAuthUser().getId().equals(id) || userRole == Role.ADMIN) {
      final JobInformationDto resultInformation = jobUserMapper.convertToJobInformationDto(target);
      resultInformation.setUserRole(targetUserRole);
      return resultInformation;
    }

    if (userRole == Role.MANAGER && target.getUser().getManagerUser() != null
        && getAuthUser().getId().equals(target.getUser().getManagerUser().getId())) {
      final JobInformationDto resultInformation = jobUserMapper.convertToJobInformationDto(target);
      resultInformation.setUserRole(targetUserRole);
      return resultInformation;
    }

    final BasicJobInformationDto resultInformation = jobUserMapper
        .convertToBasicJobInformationDto(target);
    resultInformation.setUserRole(targetUserRole);
    return resultInformation;
  }
}
