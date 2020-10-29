package shamu.company.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceDetailDto {
  private String payDate;

  private String payPeriodFrequency;

  private String periodStartDate;

  private String periodEndDate;

  private String runPayrollDeadline;
}
