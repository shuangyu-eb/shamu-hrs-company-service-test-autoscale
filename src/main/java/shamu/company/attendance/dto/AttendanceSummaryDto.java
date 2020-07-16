package shamu.company.attendance.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceSummaryDto {
  private int overTimeMinutes;

  private int workedMinutes;

  private int totalPtoMinutes;

  private double ptoPay;

  private double regHourlyPay;

  private double overTimePay;
}
