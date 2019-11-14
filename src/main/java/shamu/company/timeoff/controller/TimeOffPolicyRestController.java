package shamu.company.timeoff.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.common.exception.ForbiddenException;
import shamu.company.timeoff.dto.TimeOffAdjustmentCheckDto;
import shamu.company.timeoff.dto.TimeOffBalanceDto;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffPolicyListDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedInfoDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedUserListDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserFrontendDto;
import shamu.company.timeoff.dto.TimeOffPolicyWrapperDto;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.entity.mapper.TimeOffPolicyUserMapper;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.service.TimeOffDetailService;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

@RestApiController
public class TimeOffPolicyRestController extends BaseRestController {

  private final TimeOffPolicyService timeOffPolicyService;

  private final UserService userService;

  private final TimeOffDetailService timeOffDetailService;

  private final TimeOffPolicyUserMapper timeOffPolicyUserMapper;

  private final TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  @Autowired
  public TimeOffPolicyRestController(
      final TimeOffPolicyService timeOffPolicyService,
      final UserService userService,
      final TimeOffDetailService timeOffDetailService,
      final TimeOffPolicyUserMapper timeOffPolicyUserMapper,
      final TimeOffPolicyUserRepository timeOffPolicyUserRepository) {
    this.timeOffPolicyService = timeOffPolicyService;
    this.userService = userService;
    this.timeOffDetailService = timeOffDetailService;
    this.timeOffPolicyUserMapper = timeOffPolicyUserMapper;
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
  }

  @PostMapping("time-off-policy")
  public void createTimeOffPolicy(
      @Valid @RequestBody final TimeOffPolicyWrapperDto timeOffPolicyWrapperDto) {
    timeOffPolicyService
        .createTimeOffPolicy(timeOffPolicyWrapperDto, getCompanyId());
  }

  @PatchMapping("time-off-policy/{id}")
  @PreAuthorize("hasPermission(#id, 'TIME_OFF_POLICY', 'MANAGE_TIME_OFF_POLICY')")
  public void updateTimeOffPolicy(@Valid @PathVariable final String id,
      @RequestBody final TimeOffPolicyWrapperDto infoWrapper) {

    timeOffPolicyService.updateTimeOffPolicy(id, infoWrapper, getCompanyId());
  }

  @PatchMapping("time-off-policy/employees/{policyId}")
  @PreAuthorize("hasPermission(#policyId, 'TIME_OFF_POLICY', 'MANAGE_TIME_OFF_POLICY')")
  public void updateTimeOffPolicyEmployeesInfo(@PathVariable final String policyId,
      @RequestBody final TimeOffPolicyWrapperDto timeOffPolicyWrapperDto) {
    final List<TimeOffPolicyUserFrontendDto> timeOffPolicyUserFrontendDtos = timeOffPolicyWrapperDto
        .getUserStartBalances();
    timeOffPolicyService.updateTimeOffPolicyUserInfo(timeOffPolicyUserFrontendDtos, policyId);
  }

  @GetMapping("users/{userId}/time-off-balances")
  @PreAuthorize("hasPermission(#userId,'USER','MANAGE_USER_TIME_OFF_BALANCE') "
      + "or hasAnyAuthority('MANAGE_SELF_TIME_OFF_BALANCE')")
  public TimeOffBalanceDto getTimeOffBalances(@PathVariable final String userId) {
    final User user = userService.findUserById(userId);
    return timeOffPolicyService.getTimeOffBalances(user);
  }

  @PostMapping("time-off-policy/{policyId}/users")
  @PreAuthorize("hasPermission(#policyId, 'TIME_OFF_POLICY', 'MANAGE_TIME_OFF_POLICY')")
  public void createTimeOffPolicyUsers(@PathVariable final String policyId,
      @RequestBody final List<TimeOffPolicyUserFrontendDto> timeOffPolicyUserFrontendDtos) {
    final List<TimeOffPolicyUser> timeOffPolicyUserList = new ArrayList<>();
    timeOffPolicyUserFrontendDtos.forEach(timeOffPolicyUserFrontendDto -> timeOffPolicyUserList
        .add(timeOffPolicyUserMapper
            .createFromTimeOffPolicyUserFrontendDtoAndTimeOffPolicyId(timeOffPolicyUserFrontendDto,
                policyId)));
    timeOffPolicyService.createTimeOffPolicyUsers(timeOffPolicyUserList);
  }

  @GetMapping("users/{userId}/policy-users")
  @PreAuthorize("hasPermission(#userId, 'USER', 'VIEW_SELF') "
      + "or hasPermission(#userId, 'USER', 'MANAGE_TEAM_USER')")
  public List<TimeOffPolicyUserDto> getAllPolicyUsersByUser(
          @PathVariable final String userId,
          final Long untilDate) {
    LocalDateTime endDateTime = LocalDateTime.now();

    if (untilDate != null) {
      endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(untilDate), ZoneId.of("UTC"));
    }
    final User user = userService.findUserById(userId);
    return timeOffPolicyService.getTimeOffPolicyUser(user, endDateTime);
  }

  @GetMapping("time-off-policy/{policyId}")
  @PreAuthorize("hasPermission(#policyId, 'TIME_OFF_POLICY', 'MANAGE_TIME_OFF_POLICY')")
  public TimeOffPolicyRelatedInfoDto getTimeOffPolicyByTimeOffPolicyId(
      @PathVariable final String policyId) {
    return timeOffPolicyService.getTimeOffRelatedInfo(policyId);
  }

  @GetMapping("time-off-policies/{policyId}/users")
  @PreAuthorize("hasPermission(#policyId, 'TIME_OFF_POLICY', 'MANAGE_TIME_OFF_POLICY')")
  public TimeOffPolicyRelatedUserListDto getEmployeesByTimeOffPolicyId(
      @PathVariable final String policyId) {
    return timeOffPolicyService.getAllEmployeesByTimeOffPolicyId(policyId, getCompanyId());
  }

  @DeleteMapping("time-off/{policyId}")
  @PreAuthorize("hasPermission(#policyId, 'TIME_OFF_POLICY', 'MANAGE_TIME_OFF_POLICY')")
  public HttpEntity deleteTimeOffPolicy(@PathVariable final String policyId) {
    timeOffPolicyService.deleteTimeOffPolicy(policyId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @DeleteMapping("time-off-policies/{policyId}/{rollId}")
  @PreAuthorize("hasPermission(#policyId, 'TIME_OFF_POLICY', 'MANAGE_TIME_OFF_POLICY') "
      + "and hasPermission(#rollId, 'TIME_OFF_POLICY', 'MANAGE_TIME_OFF_POLICY')")
  public HttpEntity enrollTimeOffPolicy(@PathVariable final String policyId,
      @PathVariable final String rollId) {
    final List<TimeOffPolicyUser> deletedPolicyUsers = timeOffPolicyService
        .getAllPolicyUsersByPolicyId(policyId);
    final TimeOffPolicy enrollPolicy = timeOffPolicyService.getTimeOffPolicyById(rollId);
    timeOffPolicyService.enrollTimeOffHours(
        deletedPolicyUsers, enrollPolicy, getAuthUser().getId());
    timeOffPolicyService.deleteTimeOffPolicy(policyId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("time-off-policies")
  public List<TimeOffPolicyListDto> getAllPolicies() {
    return timeOffPolicyService.getAllPolicies(getCompanyId());
  }

  @GetMapping("time-off-balances/{policyUserId}/breakdown")
  @PreAuthorize("hasPermission(#policyUserId,"
      + "'TIME_OFF_POLICY_USER','MANAGE_USER_TIME_OFF_BALANCE') "
      + "or hasAnyAuthority('MANAGE_SELF_TIME_OFF_BALANCE')")
  public TimeOffBreakdownDto getTimeOffBreakdown(
      @PathVariable final String policyUserId,
      final Long untilDate) {
    LocalDate endDate = LocalDate.now();

    if (untilDate != null) {
      endDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(untilDate),
          ZoneOffset.UTC).toLocalDate();
    }

    final TimeOffBreakdownDto timeOffBreakdownDto = timeOffDetailService
        .getTimeOffBreakdown(policyUserId, endDate);
    timeOffBreakdownDto.setUntilDateInMillis(untilDate);
    return timeOffBreakdownDto;
  }

  @PostMapping("time-off-balances/{policyUserId}/adjustments")
  @PreAuthorize("hasPermission(#policyUserId,"
      + "'TIME_OFF_POLICY_USER','MANAGE_USER_TIME_OFF_BALANCE')")
  public void addTimeOffAdjustments(@PathVariable final String policyUserId,
      @RequestBody final Integer adjustment) {
    TimeOffAdjustmentCheckDto checkResult = timeOffDetailService
        .checkTimeOffAdjustments(policyUserId, adjustment);
    if (checkResult.getExceed()) {
      throw new ForbiddenException(String.format("Amount exceeds max balance of %s hours.",
          checkResult.getMaxBalance()));
    }

    final User currentUser = userService.findUserById(getAuthUser().getId());
    timeOffPolicyService.addTimeOffAdjustments(currentUser, policyUserId, adjustment);
  }

  @PostMapping("time-off-balances/{policyUserId}/adjustments/check")
  @PreAuthorize("hasPermission(#policyUserId,"
      + "'TIME_OFF_POLICY_USER','MANAGE_USER_TIME_OFF_BALANCE')")
  public TimeOffAdjustmentCheckDto checkTimeOffAdjustments(
      @PathVariable final String policyUserId,
      @RequestBody final Integer adjustment) {

    return timeOffDetailService.checkTimeOffAdjustments(policyUserId, adjustment);
  }

  @GetMapping("time-off-policies/users/{id}/has-policy")
  @PreAuthorize("hasPermission(#id, 'USER', 'VIEW_USER_TIME_OFF_BALANCE') "
      + "or @permissionUtils.isCurrentUserId(#id)")
  public boolean checkHasTimeOffPolicies(@PathVariable final String id) {
    return timeOffPolicyUserRepository.existsByUserId(id);
  }
}
