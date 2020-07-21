package shamu.company.attendance.dto;

import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimeEntryDto {
  private String entryId;

  private Timestamp startTime;

  private Timestamp endTime;

  private Integer hoursWorked;

  private Integer minutesWorked;

  private String comment;

  List<BreakTimeLogDto> breakTimeLogs;
}
