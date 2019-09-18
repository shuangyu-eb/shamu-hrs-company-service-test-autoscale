package shamu.company.timeoff.service;

import java.util.List;
import shamu.company.company.entity.Company;
import shamu.company.timeoff.dto.AccrualScheduleMilestoneDto;
import shamu.company.timeoff.dto.TimeOffBalanceDto;
import shamu.company.timeoff.dto.TimeOffPolicyAccrualScheduleDto;
import shamu.company.timeoff.dto.TimeOffPolicyFrontendDto;
import shamu.company.timeoff.dto.TimeOffPolicyListDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedInfoDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedUserListDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserFrontendDto;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.entity.TimeOffPolicyUser;
import shamu.company.user.entity.User;

public interface TimeOffPolicyService {

  void createTimeOffPolicy(TimeOffPolicyFrontendDto timeOffPolicyFrontendDto,
      TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualScheduleDto,
      List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList,
      List<TimeOffPolicyUserFrontendDto> timeOffPolicyUserFrontendDtos,
      Company company);

  TimeOffBalanceDto getTimeOffBalances(User user);

  List<TimeOffPolicyUserDto> getTimeOffPolicyUser(User user);

  void createTimeOffPolicyUsers(List<TimeOffPolicyUser> timeOffPolicyUsers);

  Integer getTimeOffBalanceByUserAndPolicy(User user, TimeOffPolicy timeOffPolicy);

  TimeOffPolicy getTimeOffPolicyById(Long id);

  List<TimeOffPolicyUser> getAllPolicyUsersByUser(User user);

  TimeOffPolicyRelatedInfoDto getTimeOffRelatedInfo(Long timeOffPolicyId);

  TimeOffPolicyRelatedUserListDto getAllEmployeesByTimeOffPolicyId(
      Long timeOffPolicyId, Long companyId);

  void updateTimeOffPolicy(TimeOffPolicy timeOffPolicy);

  TimeOffPolicyAccrualSchedule getTimeOffPolicyAccrualScheduleByTimeOffPolicy(
      TimeOffPolicy timeOffPolicy);

  void updateMilestones(
      List<AccrualScheduleMilestoneDto> milestones, Long scheduleId);

  void updateTimeOffPolicyUserInfo(
      List<TimeOffPolicyUserFrontendDto> userStatBalances, Long timeOffPolicyId);

  void deleteTimeOffPolicy(Long timeOffPolicyId);

  List<TimeOffPolicyUser> getAllPolicyUsersByPolicyId(Long id);

  void enrollTimeOffHours(
      List<TimeOffPolicyUser> users,TimeOffPolicy enrollPolicy,Long currentUserId);

  List<TimeOffPolicyListDto> getAllPolicies(Long companyId);

  TimeOffPolicyAccrualSchedule updateTimeOffPolicySchedule(TimeOffPolicy timeOffPolicy,
      TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualSchedule);

  List<AccrualScheduleMilestone> updateTimeOffPolicyMilestones(TimeOffPolicy timeOffPolicyUpdated,
      List<AccrualScheduleMilestoneDto> milestones);

  void addTimeOffAdjustments(User currentUser, Long policyUserId, Integer adjustment);
}
