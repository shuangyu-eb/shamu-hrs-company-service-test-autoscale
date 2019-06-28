package shamu.company.timeoff.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;

@Data
@AllArgsConstructor
public class TimeOffPolicyRelatedInfoDto {
  @HashidsFormat
  private Long id;

  private String policyName;

  private TimeOffPolicyDto timeOffPolicyDto;

  private Boolean isLimited;

  private Integer startDate;

  @HashidsFormat
  private Long timeOffAccrualFrequency;

  private Integer maxBalanceBaseRate;

  private Integer carryoverLimitBaseRate;

  private Integer accrualHoursBaseRate;

  private List<AccrualScheduleMilestone> accrualScheduleMilestone;

  public TimeOffPolicyRelatedInfoDto(
      TimeOffPolicy timeOffPolicy,
      TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule,
      List<AccrualScheduleMilestone> accrualScheduleMilestone) {
    this.id = timeOffPolicy.getId();
    this.policyName = timeOffPolicy.getName();
    this.isLimited = timeOffPolicy.getIsLimited();
    if (timeOffPolicyAccrualSchedule != null) {
      this.startDate = timeOffPolicyAccrualSchedule.getDaysBeforeAccrualStarts();
      this.timeOffAccrualFrequency =
          timeOffPolicyAccrualSchedule.getTimeOffAccrualFrequency().getId();
      this.accrualHoursBaseRate = timeOffPolicyAccrualSchedule.getAccrualHours();
      this.carryoverLimitBaseRate = timeOffPolicyAccrualSchedule.getCarryoverLimit();
      this.maxBalanceBaseRate = timeOffPolicyAccrualSchedule.getMaxBalance();
    }
    if (accrualScheduleMilestone != null) {
      this.accrualScheduleMilestone = accrualScheduleMilestone;
    }
  }
}
