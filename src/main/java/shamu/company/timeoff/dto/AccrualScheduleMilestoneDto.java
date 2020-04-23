package shamu.company.timeoff.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccrualScheduleMilestoneDto {

  private Integer anniversaryYear;

  private String timeOffPolicyAccrualScheduleId;

  @NotNull private Integer accrualHours;

  private Integer accrualInterval;

  private Integer carryoverLimit;

  private Integer maxBalance;

  private String name;
}
