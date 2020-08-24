package shamu.company.attendance.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceDetailDto {
  private String payDate;

  private String payPeriodFrequency;

  private String periodStartDate;

  private String periodEndDate;
}
