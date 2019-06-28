package shamu.company.timeoff.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import shamu.company.timeoff.dto.TimeOffPolicyAccrualScheduleDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedInfoDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedUserListDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserDto;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.pojo.TimeOffPolicyPojo;
import shamu.company.timeoff.pojo.TimeOffPolicyUserPojo;
import shamu.company.timeoff.pojo.TimeOffPolicyWrapperPojo;
import shamu.company.timeoff.service.TimeOffPolicyService;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

@RestApiController
public class TimeOffPolicyRestController extends BaseRestController {

  private final TimeOffPolicyService timeOffPolicyService;

  private final UserService userService;

  @Autowired
  public TimeOffPolicyRestController(
      TimeOffPolicyService timeOffPolicyService,
      UserService userService) {
    this.timeOffPolicyService = timeOffPolicyService;
    this.userService = userService;
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
  public void updateTimeOffPolicy(@PathVariable Long id,
      @RequestBody TimeOffPolicyWrapperPojo infoWrapper) {

    TimeOffPolicyPojo timeOffPolicyPojo = infoWrapper.getTimeOffPolicy();
    TimeOffPolicy origin = timeOffPolicyService.getTimeOffPolicyById(id);
    TimeOffPolicy timeOffPolicyUpdated = timeOffPolicyPojo.getTimeOffPolicy(origin);

    timeOffPolicyService.updateTimeOffPolicy(timeOffPolicyUpdated);
    TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
        timeOffPolicyService.getTimeOffPolicyAccrualScheduleByTimeOffPolicy(timeOffPolicyUpdated);
    TimeOffPolicyAccrualScheduleDto accrualScheduleDtoUpdated = infoWrapper
        .getTimeOffPolicyAccrualSchedule();
    TimeOffPolicyAccrualSchedule updatedSchedule = accrualScheduleDtoUpdated
        .getTimeOffPolicyAccrualScheduleUpdated(timeOffPolicyAccrualSchedule);
    timeOffPolicyService.updateTimeOffPolicyAccrualSchedule(updatedSchedule);
    List<AccrualScheduleMilestoneDto> milestoneDtos = infoWrapper.getMilestones();
    timeOffPolicyService.updateMilestones(milestoneDtos, updatedSchedule.getId());
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

  @GetMapping("time-off-balances")
  public List<TimeOffBalanceDto> getTimeOffBalances() {
    return timeOffPolicyService.getTimeOffBalances(getUser().getId(), getCompany().getId());
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

    List<TimeOffPolicyUser> timeOffPolicyUsers = timeOffPolicyService
        .getAllPolicyUsersByUser(user);
    return timeOffPolicyUsers.stream().map(policyUser -> new TimeOffPolicyUserDto(policyUser))
        .collect(Collectors.toList());
  }

  @GetMapping("time-off-policy/{policyId}")
  public TimeOffPolicyRelatedInfoDto getTimeOffPolicyByTimeOffPolicyId(
      @PathVariable Long policyId) {
    return timeOffPolicyService.getTimeOffRelatedInfo(policyId);
  }

  @GetMapping("users/{policyId}")
  public TimeOffPolicyRelatedUserListDto getEmployeesByTimeOffPolicyId(
      @PathVariable Long policyId) {
    Company company = this.getUser().getCompany();
    return timeOffPolicyService.getAllEmployeesByTimeOffPolicyId(policyId, company);
  }

  @DeleteMapping("time-off/{policyId}")
  public HttpEntity deleteTimeOffPolicy(@PathVariable @HashidsFormat Long policyId) {
    timeOffPolicyService.deleteTimeOffPolicy(policyId);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
