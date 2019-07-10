package shamu.company.timeoff.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TimeOffBreakdownMonthDto {

  private LocalDateTime date;

  private Integer accrualHours;

  private Boolean lastMonthOfTheYear;

  private TimeOffBreakdownYearDto yearData;

  public TimeOffBreakdownMonthDto(LocalDateTime date, Integer accrualHours,
      Boolean lastMonthOfTheYear) {
    this.date = date;
    this.accrualHours = accrualHours;
    this.lastMonthOfTheYear = lastMonthOfTheYear;
  }
}
