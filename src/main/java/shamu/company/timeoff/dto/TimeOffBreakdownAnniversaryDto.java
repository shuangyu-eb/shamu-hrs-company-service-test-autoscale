package shamu.company.timeoff.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class TimeOffBreakdownAnniversaryDto {

  private LocalDate date;

  private Integer accrualHours;

  private Integer maxBalance;

  private Integer carryoverLimit;

  private boolean hasParent = false;
}
