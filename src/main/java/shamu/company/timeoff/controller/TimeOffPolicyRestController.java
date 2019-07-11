package shamu.company.timeoff.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
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
import shamu.company.timeoff.dto.TimeOffPolicyListDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedInfoDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedUserListDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserDto;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.pojo.TimeOffPolicyPojo;
import shamu.company.timeoff.pojo.TimeOffPolicyUserPojo;
import shamu.company.timeoff.pojo.TimeOffPolicyWrapperPojo;
import shamu.company.timeoff.service.TimeOffDetailService;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

@RestApiController
public class TimeOffPolicyRestController extends BaseRestController {

  private final TimeOffPolicyService timeOffPolicyService;

  private final UserService userService;

  private final TimeOffDetailService timeOffDetailService;

  @Autowired
  public TimeOffPolicyRestController(
      TimeOffPolicyService timeOffPolicyService,
      UserService userService, TimeOffDetailService timeOffDetailService) {
    this.timeOffPolicyService = timeOffPolicyService;
    this.userService = userService;
    this.timeOffDetailService = timeOffDetailService;
  }

  @PostMapping("time-off-policy")
  public void createTimeOffPolicy(@RequestBody TimeOffPolicyWrapperPojo timeOffPolicyWrapperPojo) {

    TimeOffPolicyPojo timeOffPolicyPojo = timeOffPolicyWrapperPojo.getTimeOffPolicy();
    TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualScheduleDto = timeOffPolicyWrapperPojo
        .getTimeOffPolicyAccrualSchedule();
    List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList = timeOffPolicyWrapperPojo
        .getMilestones();
    List<TimeOffPolicyUserPojo> timeOffPolicyUserPojos = timeOffPolicyWrapperPojo
        .getUserStartBalances();

    Company company = this.getCompany();

    timeOffPolicyService.createTimeOffPolicy(timeOffPolicyPojo, timeOffPolicyAccrualScheduleDto,
        accrualScheduleMilestoneDtoList, timeOffPolicyUserPojos, company);
  }

  @PatchMapping("time-off-policy/{id}")
  public void updateTimeOffPolicy(@HashidsFormat @PathVariable Long id,
      @RequestBody TimeOffPolicyWrapperPojo infoWrapper) {

    TimeOffPolicyPojo timeOffPolicyPojo = infoWrapper.getTimeOffPolicy();
    TimeOffPolicy origin = timeOffPolicyService.getTimeOffPolicyById(id);
    TimeOffPolicy timeOffPolicyUpdated = timeOffPolicyPojo.getTimeOffPolicy(origin);

    timeOffPolicyService.updateTimeOffPolicy(timeOffPolicyUpdated);
    timeOffPolicyService
        .updateTimeOffPolicyMilestones(timeOffPolicyUpdated, infoWrapper.getMilestones());

    timeOffPolicyService.updateTimeOffPolicySchedule(timeOffPolicyUpdated,
        infoWrapper.getTimeOffPolicyAccrualSchedule());

    List<TimeOffPolicyUserPojo> timeOffPolicyUserPojos = infoWrapper.getUserStartBalances();
    timeOffPolicyService.updateTimeOffPolicyUserInfo(timeOffPolicyUserPojos, id);
  }

  @PatchMapping("time-off-policy/employees/{id}")
  public void updateTimeOffPolicyEmployeesInfo(@PathVariable Long id,
      @RequestBody TimeOffPolicyWrapperPojo timeOffPolicyWrapperPojo) {
    List<TimeOffPolicyUserPojo> timeOffPolicyUserPojos = timeOffPolicyWrapperPojo
        .getUserStartBalances();
    timeOffPolicyService.updateTimeOffPolicyUserInfo(timeOffPolicyUserPojos, id);

  }

  @GetMapping("users/{userId}/time-off-balances")
  @PreAuthorize("hasPermission(#userId,'USER','MANAGE_USER_TIME_OFF_BALANCE') "
      + "or hasAnyAuthority('MANAGE_SELF_TIME_OFF_BALANCE')")
  public List<TimeOffBalanceDto> getTimeOffBalances(@HashidsFormat @PathVariable Long userId) {
    User user = userService.findUserById(userId);
    return timeOffPolicyService.getTimeOffBalances(user);
  }

  @PostMapping("time-off-policy/{policyId}/users")
  public void createTimeOffPolicyUsers(@PathVariable @HashidsFormat Long policyId,
      @RequestBody List<TimeOffPolicyUserPojo> timeOffPolicyUserPojos) {
    List<TimeOffPolicyUser> timeOffPolicyUserList = new ArrayList<>();
    timeOffPolicyUserPojos.forEach(timeOffPolicyUserPojo -> timeOffPolicyUserList
        .add(timeOffPolicyUserPojo.getTimeOffPolicyUser(policyId)));
    timeOffPolicyService.createTimeOffPolicyUsers(timeOffPolicyUserList);
  }

  @GetMapping("users/{userId}/policy-users")
  public List<TimeOffPolicyUserDto> getAllPolicyUsersByUser(
      @PathVariable @HashidsFormat Long userId) {
    User user = this.userService.findUserById(userId);
    return timeOffPolicyService.getTimeOffPolicyUser(user);
  }

  @GetMapping("time-off-policy/{policyId}")
  public TimeOffPolicyRelatedInfoDto getTimeOffPolicyByTimeOffPolicyId(
      @HashidsFormat @PathVariable Long policyId) {
    return timeOffPolicyService.getTimeOffRelatedInfo(policyId);
  }

  @GetMapping("time-off-policies/{policyId}/users")
  public TimeOffPolicyRelatedUserListDto getEmployeesByTimeOffPolicyId(
      @HashidsFormat @PathVariable Long policyId) {
    Company company = this.getUser().getCompany();
    return timeOffPolicyService.getAllEmployeesByTimeOffPolicyId(policyId, company);
  }

  @DeleteMapping("time-off/{policyId}")
  public HttpEntity deleteTimeOffPolicy(@PathVariable @HashidsFormat Long policyId) {
    timeOffPolicyService.deleteTimeOffPolicy(policyId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @DeleteMapping("time-off-policies/{policyId}/{rollId}")
  public HttpEntity enrollTimeOffPolicy(@PathVariable @HashidsFormat Long policyId,
      @PathVariable @HashidsFormat Long rollId) {
    User user = this.getUser();
    List<TimeOffPolicyUser> deletedPolicyUsers = timeOffPolicyService
        .getAllPolicyUsersByPolicyId(policyId);
    TimeOffPolicy enrollPolicy = timeOffPolicyService.getTimeOffPolicyById(rollId);
    timeOffPolicyService.enrollTimeOffHours(deletedPolicyUsers,enrollPolicy,user);
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
  public TimeOffBreakdownDto getTimeOffBreakdown(@HashidsFormat @PathVariable Long policyUserId,
      Long untilDate) {
    LocalDateTime endDateTime = LocalDateTime.now();

    if (untilDate != null) {
      endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(untilDate), ZoneId.of("UTC"));
    }

    return timeOffDetailService.getTimeOffBreakdown(policyUserId, endDateTime);
  }

  @PostMapping("time-off-balances/{policyUserId}/adjustments")
  @PreAuthorize("hasPermission(#policyUserId,"
      + "'TIME_OFF_POLICY_USER','MANAGE_USER_TIME_OFF_BALANCE') "
      + "or hasAnyAuthority('MANAGE_SELF_TIME_OFF_BALANCE')")
  public void addTimeOffAdjustments(@HashidsFormat @PathVariable Long policyUserId,
      @RequestBody Integer adjustment) {
    timeOffPolicyService.addTimeOffAdjustments(getUser(), policyUserId, adjustment);
  }
}
