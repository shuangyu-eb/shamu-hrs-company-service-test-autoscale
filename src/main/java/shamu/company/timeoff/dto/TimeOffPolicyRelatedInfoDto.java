package shamu.company.timeoff.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.timeoff.entity.AccrualScheduleMilestone;
import shamu.company.timeoff.entity.TimeOffPolicy;
import shamu.company.timeoff.entity.TimeOffPolicyAccrualSchedule;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeOffPolicyRelatedInfoDto {
  private String id;

  private String policyName;

  private TimeOffPolicyDto timeOffPolicyDto;

  private Boolean isLimited;

  private Integer startDate;

  private String timeOffAccrualFrequency;

  private String timeOffAccrualFrequencyType;

  private Integer maxBalanceBaseRate;

  private Integer carryoverLimitBaseRate;

  private Integer accrualHoursBaseRate;

  private List<AccrualScheduleMilestone> accrualScheduleMilestone;

  private Boolean isAutoEnrolled;

  public TimeOffPolicyRelatedInfoDto(
      final TimeOffPolicy timeOffPolicy,
      final TimeOffPolicyAccrualSchedule timeOffPolicyAccrualSchedule,
      final List<AccrualScheduleMilestone> accrualScheduleMilestone) {
    id = timeOffPolicy.getId();
    policyName = timeOffPolicy.getName();
    isLimited = timeOffPolicy.getIsLimited();
    isAutoEnrolled = timeOffPolicy.getIsAutoEnrollEnabled();
    if (timeOffPolicyAccrualSchedule != null) {
      startDate = timeOffPolicyAccrualSchedule.getDaysBeforeAccrualStarts();
      timeOffAccrualFrequency = timeOffPolicyAccrualSchedule.getTimeOffAccrualFrequency().getId();
      timeOffAccrualFrequencyType =
          timeOffPolicyAccrualSchedule.getTimeOffAccrualFrequency().getName();
      accrualHoursBaseRate = timeOffPolicyAccrualSchedule.getAccrualHours();
      carryoverLimitBaseRate = timeOffPolicyAccrualSchedule.getCarryoverLimit();
      maxBalanceBaseRate = timeOffPolicyAccrualSchedule.getMaxBalance();
    }
    if (accrualScheduleMilestone != null) {
      this.accrualScheduleMilestone = accrualScheduleMilestone;
    }
  }
}
