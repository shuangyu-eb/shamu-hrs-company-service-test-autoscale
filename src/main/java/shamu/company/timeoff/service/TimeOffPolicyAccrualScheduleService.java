package shamu.company.timeoff.service;

import java.util.List;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;

public interface TimeOffPolicyAccrualScheduleService {
  TimeOffAccrualFrequency findTimeOffAccrualFrequencyById(Long id);

  TimeOffPolicyAccrualSchedule createTimeOffPolicyAccrualSchedule(
      TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule);

  void createAccrualScheduleMilestones(List<AccrualScheduleMilestone> accrualScheduleMilestoneList);
}
