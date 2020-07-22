package shamu.company.timeoff.controller;

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
import shamu.company.timeoff.dto.TimeOffAdjustmentCheckDto;
import shamu.company.timeoff.dto.TimeOffBalanceDto;
import shamu.company.timeoff.dto.TimeOffBreakdownDto;
import shamu.company.timeoff.dto.TimeOffPolicyListDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedInfoDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedUserDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedUserListDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserDto;
import shamu.company.timeoff.dto.TimeOffPolicyWrapperDto;
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
      final TimeOffPolicyService timeOffPolicyService,
      final UserService userService,
      final TimeOffDetailService timeOffDetailService) {
    this.timeOffPolicyService = timeOffPolicyService;
    this.userService = userService;
    this.timeOffDetailService = timeOffDetailService;
  }

  @PostMapping("time-off-policies")
  @PreAuthorize(
      "hasPermission(#timeOffPolicyWrapperDto.userStartBalances,"
          + "'TIME_OFF_USER', 'MANAGE_TIME_OFF_POLICY')")
  public ResponseEntity createTimeOffPolicy(
      @Valid @RequestBody final TimeOffPolicyWrapperDto timeOffPolicyWrapperDto) {
    timeOffPolicyService.createTimeOffPolicy(timeOffPolicyWrapperDto, findCompanyId());
    return new ResponseEntity(HttpStatus.OK);
  }

  @PatchMapping("time-off-policies/{id}")
  @PreAuthorize(
      "hasPermission(#infoWrapper.userStartBalances, 'TIME_OFF_USER', 'MANAGE_TIME_OFF_POLICY')"
          + " and hasPermission(#id, 'TIME_OFF_POLICY', 'MANAGE_TIME_OFF_POLICY')")
  public void updateTimeOffPolicy(
      @Valid @PathVariable final String id,
      @Valid @RequestBody final TimeOffPolicyWrapperDto infoWrapper) {

    timeOffPolicyService.updateTimeOffPolicy(id, infoWrapper, findCompanyId());
  }

  @PatchMapping("time-off-policies/employees/{policyId}")
  @PreAuthorize(
      "hasPermission(#timeOffPolicyWrapperDto.userStartBalances,"
          + "'TIME_OFF_USER', 'MANAGE_TIME_OFF_POLICY')"
          + " and hasPermission(#policyId, 'TIME_OFF_POLICY', 'MANAGE_TIME_OFF_POLICY')")
  public ResponseEntity updateTimeOffPolicyEmployeesInfo(
      @PathVariable final String policyId,
      @RequestBody final TimeOffPolicyWrapperDto timeOffPolicyWrapperDto) {
    timeOffPolicyService.updateTimeOffPolicyUserInfo(timeOffPolicyWrapperDto, policyId);
    return new ResponseEntity(HttpStatus.OK);
  }

  @GetMapping("time-off-policies/users/{userId}/balance")
  @PreAuthorize(
      "hasPermission(#userId,'USER','MANAGE_USER_TIME_OFF_BALANCE') "
          + "or hasAnyAuthority('MANAGE_SELF_TIME_OFF_BALANCE')")
  public TimeOffBalanceDto getTimeOffBalances(@PathVariable final String userId) {
    return timeOffPolicyService.getTimeOffBalances(userId);
  }

  @GetMapping("time-off-policies-users/users/{userId}")
  @PreAuthorize(
      "hasPermission(#userId, 'USER', 'VIEW_SELF') "
          + "or hasPermission(#userId, 'USER', 'MANAGE_TEAM_USER')")
  public List<TimeOffPolicyUserDto> getAllPolicyUsersByUser(
      @PathVariable final String userId, final Long untilDate) {
    return timeOffPolicyService.getTimeOffPolicyUser(userId, untilDate);
  }

  @GetMapping("time-off-policies/{policyId}")
  @PreAuthorize("hasPermission(#policyId, 'TIME_OFF_POLICY', 'MANAGE_TIME_OFF_POLICY')")
  public TimeOffPolicyRelatedInfoDto getTimeOffPolicyByTimeOffPolicyId(
      @PathVariable final String policyId) {
    return timeOffPolicyService.getTimeOffRelatedInfo(policyId);
  }

  @GetMapping("time-off-policies/{policyId}/users")
  @PreAuthorize("hasPermission(#policyId, 'TIME_OFF_POLICY', 'MANAGE_TIME_OFF_POLICY')")
  public TimeOffPolicyRelatedUserListDto getEmployeesByTimeOffPolicyId(
      @PathVariable final String policyId) {
    return timeOffPolicyService.getAllEmployeesByTimeOffPolicyId(policyId, findCompanyId());
  }

  @GetMapping("new-time-off-policy/users")
  @PreAuthorize("hasAnyAuthority('TIME_OFF_POLICY', 'MANAGE_TIME_OFF_POLICY')")
  public List<TimeOffPolicyRelatedUserDto> getEmployeesOfNewPolicyOrPaidHoliday() {
    return timeOffPolicyService.getEmployeesOfNewPolicyOrPaidHoliday();
  }

  @DeleteMapping("time-off-policies/{policyId}")
  @PreAuthorize("hasPermission(#policyId, 'TIME_OFF_POLICY', 'MANAGE_TIME_OFF_POLICY')")
  public HttpEntity deleteTimeOffPolicy(@PathVariable final String policyId) {
    timeOffPolicyService.deleteTimeOffPolicy(policyId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @DeleteMapping("time-off-policies/{policyId}/{rollId}")
  @PreAuthorize(
      "hasPermission(#policyId, 'TIME_OFF_POLICY', 'MANAGE_TIME_OFF_POLICY') "
          + "and hasPermission(#rollId, 'TIME_OFF_POLICY', 'MANAGE_TIME_OFF_POLICY')")
  public HttpEntity enrollTimeOffPolicy(
      @PathVariable final String policyId, @PathVariable final String rollId) {
    timeOffPolicyService.enrollTimeOffHours(policyId, rollId, findAuthUser().getId());
    timeOffPolicyService.deleteTimeOffPolicy(policyId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("time-off-policies")
  public List<TimeOffPolicyListDto> getAllPolicies() {
    return timeOffPolicyService.getAllPolicies();
  }

  @GetMapping("time-off-balances/{policyUserId}/breakdown")
  @PreAuthorize(
      "hasPermission(#policyUserId,"
          + "'TIME_OFF_POLICY_USER','MANAGE_USER_TIME_OFF_BALANCE') "
          + "or hasAnyAuthority('MANAGE_SELF_TIME_OFF_BALANCE')")
  public TimeOffBreakdownDto getTimeOffBreakdown(
      @PathVariable final String policyUserId, final Long untilDate) {
    final TimeOffBreakdownDto timeOffBreakdownDto =
        timeOffDetailService.getTimeOffBreakdown(policyUserId, untilDate);
    timeOffBreakdownDto.setUntilDateInMillis(untilDate);
    return timeOffBreakdownDto;
  }

  @PostMapping("time-off-balances/{policyUserId}/adjustments")
  @PreAuthorize(
      "hasPermission(#policyUserId," + "'TIME_OFF_POLICY_USER','MANAGE_USER_TIME_OFF_BALANCE')")
  public void addTimeOffAdjustments(
      @PathVariable final String policyUserId, @RequestBody final Integer newBalance) {
    final User currentUser = userService.findById(findAuthUser().getId());
    timeOffPolicyService.addTimeOffAdjustments(currentUser, policyUserId, newBalance);
  }

  @PostMapping("time-off-policies-users/{policyUserId}/adjustments/check")
  @PreAuthorize(
      "hasPermission(#policyUserId," + "'TIME_OFF_POLICY_USER','MANAGE_USER_TIME_OFF_BALANCE')")
  public TimeOffAdjustmentCheckDto checkTimeOffAdjustments(
      @PathVariable final String policyUserId, @RequestBody final Integer newBalance) {

    return timeOffDetailService.checkTimeOffAdjustments(policyUserId, newBalance);
  }

  @GetMapping("time-off-policies/users/{id}/has-policy")
  @PreAuthorize(
      "hasPermission(#id, 'USER', 'VIEW_USER_TIME_OFF_BALANCE') "
          + "or @permissionUtils.isCurrentUserId(#id)")
  public boolean checkHasTimeOffPolicies(@PathVariable final String id) {
    return timeOffPolicyService.checkHasTimeOffPolicies(id);
  }
}
