package shamu.company.timeoff.service;

import java.util.List;
import shamu.company.company.entity.Company;
import shamu.company.timeoff.dto.AccrualScheduleMilestoneDto;
import shamu.company.timeoff.dto.TimeOffBalanceDto;
import shamu.company.timeoff.dto.TimeOffPolicyAccrualScheduleDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedInfoDto;
import shamu.company.timeoff.dto.TimeOffPolicyRelatedUserListDto;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
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

  Integer getTimeOffBalanceByUserId(Long userId);

  TimeOffPolicy getTimeOffPolicyById(Long id);

  List<TimeOffPolicyUser> getAllPolicyUsersByUser(User user);

  TimeOffPolicyRelatedInfoDto getTimeOffRelatedInfo(Long timeOffPolicyId);

  TimeOffPolicyRelatedUserListDto getAllEmployeesByTimeOffPolicyId(
      Long timeOffPolicyId, Company company);

  void updateTimeOffPolicy(TimeOffPolicy timeOffPolicy);

  TimeOffPolicyAccrualSchedule getTimeOffPolicyAccrualScheduleByTimeOffPolicy(
      TimeOffPolicy timeOffPolicy);

  void updateTimeOffPolicyAccrualSchedule(
      TimeOffPolicyAccrualSchedule schedule);

  void updateMilestones(
      List<AccrualScheduleMilestoneDto> milestones, Long scheduleId);

  void updateTimeOffPolicyUserInfo(
      List<TimeOffPolicyUserPojo> userStatBalances, Long timeOffPolicyId);

  void deleteTimeOffPolicy(Long timeOffPolicyId);
}
