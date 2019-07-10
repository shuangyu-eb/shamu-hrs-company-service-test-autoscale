package shamu.company.timeoff.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TimeOffBreakdownAnniversaryDto {

  private LocalDateTime date;

  private Integer accrualHours;

  private Integer maxBalance;

  private Integer carryoverLimit;
}
