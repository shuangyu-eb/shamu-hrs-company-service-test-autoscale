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

@RestApiController
public class EmployeeInformationRestController extends BaseRestController {

  private final UserService userService;

  private final EmployeeService employeeService;

  private final JobUserService jobUserService;

  private final JobUserMapper jobUserMapper;

  private final UserMapper userMapper;

  private final UserContactInformationMapper userContactInformationMapper;

  private final UserPersonalInformationMapper userPersonalInformationMapper;

  @Autowired
  public EmployeeInformationRestController(final UserService userService,
      EmployeeService employeeService,
      final JobUserService jobUserService,
      final JobUserMapper jobUserMapper,
      final UserContactInformationMapper userContactInformationMapper,
      final UserPersonalInformationMapper userPersonalInformationMapper,
      final UserMapper userMapper) {
    this.userService = userService;
    this.employeeService = employeeService;
    this.jobUserService = jobUserService;
    this.jobUserMapper = jobUserMapper;
    this.userContactInformationMapper = userContactInformationMapper;
    this.userPersonalInformationMapper = userPersonalInformationMapper;
    this.userMapper = userMapper;
  }

  @GetMapping("/employees/{id}/info")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_USER_PERSONAL')")
  public EmployeeRelatedInformationDto getEmployeeInfoByUserId(
      @PathVariable @HashidsFormat final Long id) {
    final User employee = userService.findEmployeeInfoByUserId(id);
    final Status userStatus = employee.getUserStatus().getStatus();

    Timestamp sendDate = null;
    if (userStatus == Status.PENDING_VERIFICATION) {
      Email email = employeeService.getWelcomeEmail(employee.getEmailWork());
      sendDate = email != null ? email.getSendDate() : null;
    }

    JobUserDto managerjobUserDto = null;
    if (employee.getManagerUser() != null) {
      managerjobUserDto =
          userService.findEmployeeInfoByEmployeeId(employee.getManagerUser().getId());
    }
    final JobUserDto jobUserDto = userService.findEmployeeInfoByEmployeeId(id);

    final List<JobUserDto> reports = userService.findDirectReportsByManagerId(id).stream()
        .map((user) -> userService.findEmployeeInfoByEmployeeId(user.getId()))
        .collect(Collectors.toList());

    return jobUserMapper.convertToEmployeeRelatedInformationDto(id, employee.getEmailWork(),
        userStatus.name(), sendDate, jobUserDto,
        managerjobUserDto, reports);
  }

  @GetMapping("users/{id}/personal")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_USER_PERSONAL')")
  public BasicUserPersonalInformationDto getPersonalMessage(
      @PathVariable @HashidsFormat final Long id) {
    final User user = getUser();
    final User targetUser = userService.findUserById(id);
    final UserPersonalInformation personalInformation = targetUser.getUserPersonalInformation();

    // The user's full personal message can only be accessed by admin and himself.
    if (user.getId().equals(id) || user.getRole() == Role.ADMIN) {
      return userPersonalInformationMapper
          .convertToEmployeePersonalInformationDto(personalInformation);
    }
    if (targetUser.getManagerUser().getId().equals(user.getId())) {
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
    final User user = getUser();
    final User targetUser = userService.findUserById(id);
    final UserContactInformation contactInformation = targetUser.getUserContactInformation();

    // The user's full contact message can only be accessed by admin, the manager and himself.
    if (user.getId().equals(id)
        || targetUser.getManagerUser().getId().equals(user.getId())
        || user.getRole() == Role.ADMIN) {
      return userContactInformationMapper
          .convertToEmployeeContactInformationDto(contactInformation);
    }

    return userContactInformationMapper.convertToBasicUserContactInformationDto(contactInformation);
  }

  @GetMapping("users/{id}/job")
  @PreAuthorize("hasPermission(#id,'USER','VIEW_USER_JOB')")
  public BasicJobInformationDto getJobMessage(@PathVariable @HashidsFormat final Long id) {
    final User user = getUser();
    final JobUser target = jobUserService.getJobUserByUserId(id);

    if (target == null) {
      final User targetUser = userService.findUserById(id);
      return userMapper.convertToBasicJobInformationDto(targetUser);
    }

    // The user's full job message can only be accessed by admin, the manager and himself.
    if (user.getId().equals(id) || user.getRole() == Role.ADMIN) {
      return jobUserMapper.convertToJobInformationDto(target);
    }

    return jobUserMapper.convertToBasicJobInformationDto(target);
  }
}
