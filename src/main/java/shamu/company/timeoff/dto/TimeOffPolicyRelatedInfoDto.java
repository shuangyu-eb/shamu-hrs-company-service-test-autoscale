package shamu.company.timeoff.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;

@Data
@AllArgsConstructor
public class TimeOffPolicyRelatedInfoDto {
  private String id;

  private String policyName;

  private TimeOffPolicyDto timeOffPolicyDto;

  private Boolean isLimited;

  private Integer startDate;

  private String timeOffAccrualFrequency;

  private Integer maxBalanceBaseRate;

  private Integer carryoverLimitBaseRate;

  private Integer accrualHoursBaseRate;

  private List<AccrualScheduleMilestone> accrualScheduleMilestone;

  public TimeOffPolicyRelatedInfoDto(
      final TimeOffPolicy timeOffPolicy,
      final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule,
      final List<AccrualScheduleMilestone> accrualScheduleMilestone) {
    id = timeOffPolicy.getId();
    policyName = timeOffPolicy.getName();
    isLimited = timeOffPolicy.getIsLimited();
    if (timeOffPolicyAccrualSchedule != null) {
      startDate = timeOffPolicyAccrualSchedule.getDaysBeforeAccrualStarts();
      timeOffAccrualFrequency =
          timeOffPolicyAccrualSchedule.getTimeOffAccrualFrequency().getId();
      accrualHoursBaseRate = timeOffPolicyAccrualSchedule.getAccrualHours();
      carryoverLimitBaseRate = timeOffPolicyAccrualSchedule.getCarryoverLimit();
      maxBalanceBaseRate = timeOffPolicyAccrualSchedule.getMaxBalance();
    }
    if (accrualScheduleMilestone != null) {
      this.accrualScheduleMilestone = accrualScheduleMilestone;
    }
  }
}
