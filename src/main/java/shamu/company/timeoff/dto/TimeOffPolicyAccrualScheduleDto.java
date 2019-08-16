package shamu.company.timeoff.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class TimeOffPolicyAccrualScheduleDto {

  @HashidsFormat
  private Long id;

  @NotNull
  private Integer accrualHours;

  private Integer maxBalance;

  private Integer carryoverLimit;

  private Integer daysBeforeAccrualStarts;

  private TimeOffPolicyDto timeOffPolicyDto;

  @HashidsFormat
  private Long timeOffAccrualFrequencyId;
}
