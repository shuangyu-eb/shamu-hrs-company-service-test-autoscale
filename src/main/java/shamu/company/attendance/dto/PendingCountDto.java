package shamu.company.attendance.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PendingCountDto {
  private Integer teamHoursPendingCount;

  private Integer companyHoursPendingCount;
}
