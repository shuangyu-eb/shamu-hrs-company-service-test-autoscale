package shamu.company.timeoff.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shamu.company.company.entity.Company;
import shamu.company.timeoff.dto.AccrualScheduleMilestoneDto;
import shamu.company.timeoff.dto.TimeOffBalanceDto;
import shamu.company.timeoff.dto.TimeOffPolicyAccrualScheduleDto;
import shamu.company.timeoff.dto.TimeOffPolicyDto;
import shamu.company.timeoff.dto.TimeOffPolicyUserDto;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;
import shamu.company.timeoff.repository.AccrualScheduleMilestoneRepository;
import shamu.company.timeoff.repository.TimeOffPolicyAccrualScheduleRepository;
import shamu.company.timeoff.repository.TimeOffPolicyRepository;
import shamu.company.timeoff.repository.TimeOffPolicyUserRepository;
import shamu.company.timeoff.service.TimeOffPolicyService;

@Service
public class TimeOffPolicyServiceImpl implements TimeOffPolicyService {

  private final TimeOffPolicyRepository timeOffPolicyRepository;

  private final TimeOffPolicyUserRepository timeOffPolicyUserRepository;

  private final AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository;

  private final TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository;

  @Autowired
  public TimeOffPolicyServiceImpl(
      TimeOffPolicyRepository timeOffPolicyRepository,
      TimeOffPolicyUserRepository timeOffPolicyUserRepository,
      AccrualScheduleMilestoneRepository accrualScheduleMilestoneRepository,
      TimeOffPolicyAccrualScheduleRepository timeOffPolicyAccrualScheduleRepository) {
    this.timeOffPolicyRepository = timeOffPolicyRepository;
    this.timeOffPolicyUserRepository = timeOffPolicyUserRepository;
    this.accrualScheduleMilestoneRepository = accrualScheduleMilestoneRepository;
    this.timeOffPolicyAccrualScheduleRepository = timeOffPolicyAccrualScheduleRepository;
  }

  @Override
  public void createTimeOffPolicy(TimeOffPolicyDto timeOffPolicyDto,
      TimeOffPolicyAccrualScheduleDto timeOffPolicyAccrualScheduleDto,
      List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList,
      List<TimeOffPolicyUserDto> timeOffPolicyUserDtoList,
      Company company) {

    TimeOffPolicy timeOffPolicy = timeOffPolicyRepository
        .save(timeOffPolicyDto.getTimeOffPolicy(company));
    Long policyId = timeOffPolicy.getId();
    Long timeOffAccrualFrequencyId = timeOffPolicyAccrualScheduleDto.getTimeOffAccrualFrequencyId();

    if (timeOffPolicyDto.getIsLimited()) {
      TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule =
          timeOffPolicyAccrualScheduleRepository
              .save(timeOffPolicyAccrualScheduleDto
                  .getTimeOffPolicyAccrualSchedule(timeOffPolicy, timeOffAccrualFrequencyId));

      Long scheduleId = timeOffPolicyAccrualSchedule.getId();
      createAccrualScheduleMilestones(accrualScheduleMilestoneDtoList,scheduleId);
    }

    createTimeOffPolicyUsers(timeOffPolicyUserDtoList,policyId);
  }

  @Override
  public List<TimeOffBalanceDto> getTimeOffBalances(Long userId, Long companyId) {
    return timeOffPolicyUserRepository
        .findTimeOffBalancesByUser(userId, companyId)
        .stream().map((timeOffBalance -> {
          TimeOffBalanceDto timeOffBalanceDto = new TimeOffBalanceDto();
          BeanUtils.copyProperties(timeOffBalance, timeOffBalanceDto);
          return timeOffBalanceDto;
        })).collect(Collectors.toList());
  }

  private void createTimeOffPolicyUsers(List<TimeOffPolicyUserDto> timeOffPolicyUserDtoList,
      Long policyId) {
    if (!timeOffPolicyUserDtoList.isEmpty()) {
      timeOffPolicyUserRepository.saveAll(
          timeOffPolicyUserDtoList
              .stream()
              .map(timeOffPolicyUserDto ->
                  timeOffPolicyUserDto.getTimeOffPolicyUser(policyId))
              .collect(Collectors.toList())
      );
    }
  }

  private void createAccrualScheduleMilestones(
      List<AccrualScheduleMilestoneDto> accrualScheduleMilestoneDtoList, Long scheduleId) {
    if (!accrualScheduleMilestoneDtoList.isEmpty()) {
      accrualScheduleMilestoneRepository.saveAll(
          accrualScheduleMilestoneDtoList
              .stream()
              .map(accrualScheduleMilestoneDto ->
                  accrualScheduleMilestoneDto.getAccrualScheduleMilestone(scheduleId))
              .collect(Collectors.toList()));
    }
  }
}
