package shamu.company.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllTimeDto {
  private String logId;

  private String startTime;

  private int duration;

  private String timeType;

  private OvertimeDetailDto overtimeDetails;
}
