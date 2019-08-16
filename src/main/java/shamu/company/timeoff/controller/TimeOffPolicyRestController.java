package shamu.company.timeoff.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import org.apache.commons.lang.BooleanUtils;
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
import shamu.company.company.entity.Company;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.dto.AccrualScheduleMilestoneDto;
import shamu.company.timeoff.dto.TimeOffBalanceDto;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffPolicyAccrualScheduleDto;
import shamu.company.timeoff.dto.TimeOffPolicyFrontendDto;
import shamu.company.timeoff.dto.TimeOffPolicyListDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedInfoDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedUserListDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserFrontendDto;
import shamu.company.timeoff.dto.TimeOffPolicyWrapperDto;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.entity.mapper.TimeOffPolicyMapper;
import shamu.company.timeoff.entity.mapper.TimeOffPolicyUserMapper;
import shamu.company.timeoff.service.TimeOffDetailService;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

@RestApiController
public class TimeOffPolicyRestController extends BaseRestController {

  private final TimeOffPolicyService timeOffPolicyService;

  private final UserService userService;

  private final TimeOffDetailService timeOffDetailService;

  private final TimeOffPolicyMapper timeOffPolicyMapper;

  private final TimeOffPolicyUserMapper timeOffPolicyUserMapper;

  @Autowired
  public TimeOffPolicyRestController(
      final TimeOffPolicyService timeOffPolicyService,
      final UserService userService,
      final TimeOffDetailService timeOffDetailService,
      final TimeOffPolicyMapper timeOffPolicyMapper,
      final TimeOffPolicyUserMapper timeOffPolicyUserMapper) {
    this.timeOffPolicyService = timeOffPolicyService;
    this.userService = userService;
    this.timeOffDetailService = timeOffDetailService;
    this.timeOffPolicyMapper = timeOffPolicyMapper;
    this.timeOffPolicyUserMapper = timeOffPolicyUserMapper;
  }

  @PostMapping("time-off-policy")
  public void createTimeOffPolicy(
      @Valid @RequestBody final TimeOffPolicyWrapperDto timeOffPolicyWrapperDto) {

    final TimeOffPolicyFrontendDto timeOffPolicyFrontendDto = timeOffPolicyWrapperDto
        .getTimeOffPolicy();
    final TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualScheduleDto = timeOffPolicyWrapperDto
        .getTimeOffPolicyAccrualSchedule();
    final List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList =
        timeOffPolicyWrapperDto.getMilestones();
    final List<TimeOffPolicyUserFrontendDto> timeOffPolicyUserFrontendDtos = timeOffPolicyWrapperDto
        .getUserStartBalances();

    final Company company = this.getCompany();

    timeOffPolicyService
        .createTimeOffPolicy(timeOffPolicyFrontendDto, timeOffPolicyAccrualScheduleDto,
            accrualScheduleMilestoneDtoList, timeOffPolicyUserFrontendDtos, company);
  }

  @PatchMapping("time-off-policy/{id}")
  public void updateTimeOffPolicy(@Valid @HashidsFormat @PathVariable final Long id,
      @RequestBody final TimeOffPolicyWrapperDto infoWrapper) {

    final TimeOffPolicyFrontendDto timeOffPolicyFrontendDto = infoWrapper.getTimeOffPolicy();
    final TimeOffPolicy origin = timeOffPolicyService.getTimeOffPolicyById(id);
    timeOffPolicyMapper.updateFromTimeOffPolicyFrontendDto(origin, timeOffPolicyFrontendDto);

    timeOffPolicyService.updateTimeOffPolicy(origin);

    if (BooleanUtils.isTrue(origin.getIsLimited())) {
      timeOffPolicyService
          .updateTimeOffPolicyMilestones(origin, infoWrapper.getMilestones());

      timeOffPolicyService.updateTimeOffPolicySchedule(origin,
          infoWrapper.getTimeOffPolicyAccrualSchedule());
    }

    final List<TimeOffPolicyUserFrontendDto> timeOffPolicyUserFrontendDtos = infoWrapper
        .getUserStartBalances();
    timeOffPolicyService.updateTimeOffPolicyUserInfo(timeOffPolicyUserFrontendDtos, id);
  }

  @PatchMapping("time-off-policy/employees/{id}")
  public void updateTimeOffPolicyEmployeesInfo(@HashidsFormat @PathVariable final Long id,
      @RequestBody final TimeOffPolicyWrapperDto timeOffPolicyWrapperDto) {
    final List<TimeOffPolicyUserFrontendDto> timeOffPolicyUserFrontendDtos = timeOffPolicyWrapperDto
        .getUserStartBalances();
    timeOffPolicyService.updateTimeOffPolicyUserInfo(timeOffPolicyUserFrontendDtos, id);
  }

  @GetMapping("users/{userId}/time-off-balances")
  @PreAuthorize("hasPermission(#userId,'USER','MANAGE_USER_TIME_OFF_BALANCE') "
      + "or hasAnyAuthority('MANAGE_SELF_TIME_OFF_BALANCE')")
  public TimeOffBalanceDto getTimeOffBalances(@HashidsFormat @PathVariable final Long userId) {
    final User user = userService.findUserById(userId);
    return timeOffPolicyService.getTimeOffBalances(user);
  }

  @PostMapping("time-off-policy/{policyId}/users")
  public void createTimeOffPolicyUsers(@PathVariable @HashidsFormat final Long policyId,
      @RequestBody final List<TimeOffPolicyUserFrontendDto> timeOffPolicyUserFrontendDtos) {
    final List<TimeOffPolicyUser> timeOffPolicyUserList = new ArrayList<>();
    timeOffPolicyUserFrontendDtos.forEach(timeOffPolicyUserFrontendDto -> timeOffPolicyUserList
        .add(timeOffPolicyUserMapper
            .createFromTimeOffPolicyUserFrontendDtoAndTimeOffPolicyId(timeOffPolicyUserFrontendDto,
                policyId)));
    timeOffPolicyService.createTimeOffPolicyUsers(timeOffPolicyUserList);
  }

  @GetMapping("users/{userId}/policy-users")
  public List<TimeOffPolicyUserDto> getAllPolicyUsersByUser(
      @PathVariable @HashidsFormat final Long userId) {
    final User user = this.userService.findUserById(userId);
    return timeOffPolicyService.getTimeOffPolicyUser(user);
  }

  @GetMapping("time-off-policy/{policyId}")
  public TimeOffPolicyRelatedInfoDto getTimeOffPolicyByTimeOffPolicyId(
      @HashidsFormat @PathVariable final Long policyId) {
    return timeOffPolicyService.getTimeOffRelatedInfo(policyId);
  }

  @GetMapping("time-off-policies/{policyId}/users")
  public TimeOffPolicyRelatedUserListDto getEmployeesByTimeOffPolicyId(
      @HashidsFormat @PathVariable final Long policyId) {
    final Company company = this.getUser().getCompany();
    return timeOffPolicyService.getAllEmployeesByTimeOffPolicyId(policyId, company);
  }

  @DeleteMapping("time-off/{policyId}")
  public HttpEntity deleteTimeOffPolicy(@PathVariable @HashidsFormat final Long policyId) {
    timeOffPolicyService.deleteTimeOffPolicy(policyId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @DeleteMapping("time-off-policies/{policyId}/{rollId}")
  public HttpEntity enrollTimeOffPolicy(@PathVariable @HashidsFormat final Long policyId,
      @PathVariable @HashidsFormat final Long rollId) {
    final User user = this.getUser();
    final List<TimeOffPolicyUser> deletedPolicyUsers = timeOffPolicyService
        .getAllPolicyUsersByPolicyId(policyId);
    final TimeOffPolicy enrollPolicy = timeOffPolicyService.getTimeOffPolicyById(rollId);
    timeOffPolicyService.enrollTimeOffHours(deletedPolicyUsers, enrollPolicy, user);
    timeOffPolicyService.deleteTimeOffPolicy(policyId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("time-off-policies")
  public List<TimeOffPolicyListDto> getAllPolicies() {
    return timeOffPolicyService.getAllPolicies(getCompany().getId());
  }

  @GetMapping("time-off-balances/{policyUserId}/breakdown")
  @PreAuthorize("hasPermission(#policyUserId,"
      + "'TIME_OFF_POLICY_USER','MANAGE_USER_TIME_OFF_BALANCE') "
      + "or hasAnyAuthority('MANAGE_SELF_TIME_OFF_BALANCE')")
  public TimeOffBreakdownDto getTimeOffBreakdown(
      @HashidsFormat @PathVariable final Long policyUserId,
      final Long untilDate) {
    LocalDateTime endDateTime = LocalDateTime.now();

    if (untilDate != null) {
      endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(untilDate), ZoneId.of("UTC"));
    }

    return timeOffDetailService.getTimeOffBreakdown(policyUserId, endDateTime);
  }

  @PostMapping("time-off-balances/{policyUserId}/adjustments")
  @PreAuthorize("hasPermission(#policyUserId,"
      + "'TIME_OFF_POLICY_USER','MANAGE_USER_TIME_OFF_BALANCE')")
  public void addTimeOffAdjustments(@HashidsFormat @PathVariable final Long policyUserId,
      @RequestBody final Integer adjustment) {
    timeOffPolicyService.addTimeOffAdjustments(getUser(), policyUserId, adjustment);
  }
}
