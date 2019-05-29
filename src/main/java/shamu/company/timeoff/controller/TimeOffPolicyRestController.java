package shamu.company.timeoff.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
import shamu.company.timeoff.dto.TimeOffPolicyUserDto;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.pojo.TimeOffPolicyPojo;
import shamu.company.timeoff.pojo.TimeOffPolicyUserPojo;
import shamu.company.timeoff.pojo.TimeOffPolicyWrapperPojo;
import shamu.company.timeoff.service.TimeOffPolicyService;

@RestApiController
public class TimeOffPolicyRestController extends BaseRestController {

  private final TimeOffPolicyService timeOffPolicyService;

  @Autowired
  public TimeOffPolicyRestController(
      TimeOffPolicyService timeOffPolicyService) {
    this.timeOffPolicyService = timeOffPolicyService;
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

  @GetMapping("users/{id}/policy-users")
  public List<TimeOffPolicyUserDto> getAllPolicyUsersByUser() {
    List<TimeOffPolicyUser> timeOffPolicyUsers = timeOffPolicyService
        .getAllPolicyUsersByUser(this.getUser());
    return timeOffPolicyUsers.stream().map(policyUser -> new TimeOffPolicyUserDto(policyUser))
        .collect(Collectors.toList());
  }
}
