package shamu.company.timeoff.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.common.exception.ResourceNotFoundException;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffAccrualFrequency;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;
import shamu.company.timeoff.repository.TimeOffAccrualFrequencyRepository;
import shamu.company.timeoff.repository.TimeOffPolicyAccrualScheduleRepository;
import shamu.company.timeoff.service.TimeOffPolicyAccrualScheduleService;

@Service
public class TimeOffPolicyAccrualScheduleServiceImpl implements
    TimeOffPolicyAccrualScheduleService {

  private final TimeOffAccrualFrequencyRepository timeOffAccrualFrequencyRepository;

  private final TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository;

  private final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  public TimeOffPolicyAccrualScheduleServiceImpl(
      TimeOffAccrualFrequencyRepository timeOffAccrualFrequencyRepository,
      TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository,
      AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository) {
    this.timeOffAccrualFrequencyRepository = timeOffAccrualFrequencyRepository;
    this.timeOffPolicyAccrualScheduleRepository = timeOffPolicyAccrualScheduleRepository;
    this.accrualScheduleMilestoneRepository = accrualScheduleMilestoneRepository;
  }

  @Override
  public TimeOffAccrualFrequency findTimeOffAccrualFrequencyById(Long id) {
    return timeOffAccrualFrequencyRepository.findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("Time off accrual frequency doesn't exist"));
  }

  @Override
  public TimeOffPolicyAccrualSchedule createTimeOffPolicyAccrualSchedule(
      TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule) {
    return timeOffPolicyAccrualScheduleRepository.save(timeOffPolicyAccrualSchedule);
  }

  @Override
  public void createAccrualScheduleMilestones(
      List<AccrualScheduleMilestone> accrualScheduleMilestoneList) {
    accrualScheduleMilestoneRepository.saveAll(accrualScheduleMilestoneList);
  }
}
