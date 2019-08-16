package shamu.company.timeoff.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class AccrualScheduleMilestoneDto {

  private Integer anniversaryYear;

  @HashidsFormat
  private Long timeOffPolicyAccrualScheduleId;

  @NotNull
  private Integer accrualHours;

  private Integer accrualInterval;

  private Integer carryoverLimit;

  private Integer maxBalance;

  private String name;
}
