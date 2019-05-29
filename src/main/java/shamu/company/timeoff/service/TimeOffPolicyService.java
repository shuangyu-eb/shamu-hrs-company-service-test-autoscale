package shamu.company.timeoff.service;

import java.util.List;
import shamu.company.company.entity.Company;
import shamu.company.timeoff.dto.AccrualScheduleMilestoneDto;
import shamu.company.timeoff.dto.TimeOffBalanceDto;
import shamu.company.timeoff.dto.TimeOffPolicyAccrualScheduleDto;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.timeoff.pojo.TimeOffPolicyPojo;
import shamu.company.timeoff.pojo.TimeOffPolicyUserPojo;
import shamu.company.user.entity.User;

public interface TimeOffPolicyService {

  void createTimeOffPolicy(TimeOffPolicyPojo timeOffPolicyPojo,
      TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualScheduleDto,
      List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList,
      List<TimeOffPolicyUserPojo> timeOffPolicyUserPojos,
      Company company);

  List<TimeOffBalanceDto> getTimeOffBalances(Long userId, Long companyId);

  void createTimeOffPolicyUsers(List<TimeOffPolicyUser> timeOffPolicyUsers);

  TimeOffPolicy getTimeOffPolicyById(Long id);

  List<TimeOffPolicyUser> getAllPolicyUsersByUser(User user);

  TimeOffPolicyUser updateTimeOffBalance(Long timeOffPolicyUserId, Integer totalHours);
}
