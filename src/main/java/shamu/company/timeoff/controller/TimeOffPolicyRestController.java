package shamu.company.timeoff.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.common.BaseRestController;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.company.entity.Company;
import shamu.company.timeoff.dto.AccrualScheduleMilestoneDto;
import shamu.company.timeoff.dto.TimeOffBalanceDto;
import shamu.company.timeoff.dto.TimeOffPolicyAccrualScheduleDto;
import shamu.company.timeoff.dto.TimeOffPolicyDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserDto;
import shamu.company.timeoff.pojo.TimeOffPolicyPojo;
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
  public void createTimeOffPolicy(@RequestBody TimeOffPolicyPojo timeOffPolicyPojo) {

    TimeOffPolicyDto timeOffPolicyDto = timeOffPolicyPojo.getTimeOffPolicy();
    TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualScheduleDto = timeOffPolicyPojo
        .getTimeOffPolicyAccrualSchedule();
    List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList = timeOffPolicyPojo
        .getMilestones();
    List<TimeOffPolicyUserDto> timeOffPolicyUserDtoList = timeOffPolicyPojo.getUserStartBalances();

    Company company = this.getCompany();

    timeOffPolicyService.createTimeOffPolicy(timeOffPolicyDto, timeOffPolicyAccrualScheduleDto,
        accrualScheduleMilestoneDtoList, timeOffPolicyUserDtoList, company);
  }

  @GetMapping("time-off-balances")
  public List<TimeOffBalanceDto> getTimeOffBalances() {
    return timeOffPolicyService.getTimeOffBalances(getUser().getId(), getCompany().getId());
  }
}
