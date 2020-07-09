package shamu.company.attendance.dto;

import lombok.Data;

@Data
public class MyHoursTimeLogDto {
  private String logId;

  private String startTime;

  private int duration;

  private String timeType;

  private String basePay;
}
