package shamu.company.attendance.dto;

import lombok.Data;

@Data
public class PayDetailDto {

  private String timeLogId;

  private String timeRange;

  private String minutes;

  private String pay;

  private String timeType;
}
