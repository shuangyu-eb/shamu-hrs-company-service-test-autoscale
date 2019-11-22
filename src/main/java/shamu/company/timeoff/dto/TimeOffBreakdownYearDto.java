package shamu.company.timeoff.dto;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class TimeOffBreakdownYearDto {

  private LocalDate date;

  private Integer maxBalance;

  private Integer carryoverLimit;

  private Integer accrualHours;

  private boolean hasParent = false;
}
