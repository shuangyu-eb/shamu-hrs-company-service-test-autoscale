package shamu.company.timeoff.dto;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TimeOffBreakdownMonthDto {

  private LocalDate date;

  private Integer accrualHours;

  private Boolean lastMonthOfPreviousAnniversaryYear = false;

  private TimeOffBreakdownYearDto yearData;

  private boolean hasParent = false;

  public TimeOffBreakdownMonthDto(LocalDate date, Integer accrualHours) {
    this.date = date;
    this.accrualHours = accrualHours;
  }
}
