package shamu.company.timeoff.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TimeOffPolicyAccrualScheduleDto {

  private String id;

  @NotNull
  private Integer accrualHours;

  private Integer maxBalance;

  private Integer carryoverLimit;

  private Integer daysBeforeAccrualStarts;

  private TimeOffPolicyDto timeOffPolicyDto;

  private String timeOffAccrualFrequencyId;
}
