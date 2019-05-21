package shamu.company.timeoff.service;

import java.util.List;
import shamu.company.company.entity.Company;
import shamu.company.timeoff.dto.AccrualScheduleMilestoneDto;
import shamu.company.timeoff.dto.TimeOffPolicyAccrualScheduleDto;
import shamu.company.timeoff.dto.TimeOffPolicyDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserDto;

public interface TimeOffPolicyService {

  void createTimeOffPolicy(TimeOffPolicyDto timeOffPolicyDto,
      TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualScheduleDto,
      List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList,
      List<TimeOffPolicyUserDto> timeOffPolicyUserDtoList,
      Company company);
}
