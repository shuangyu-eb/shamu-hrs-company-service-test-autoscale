package shamu.company.attendance.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceSummaryDto {
  private String overTimeHours;

  private String timeOffHours;

  private String paidHours;

  private String grossPay;
}
