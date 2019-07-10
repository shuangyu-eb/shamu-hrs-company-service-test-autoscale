package shamu.company.timeoff.dto;

import lombok.Data;

@Data
public class TimeOffBreakdownYearDto {

  private Integer year;

  private Integer maxBalance;

  private Integer carryoverLimit;

  private Integer accrualHours;
}
