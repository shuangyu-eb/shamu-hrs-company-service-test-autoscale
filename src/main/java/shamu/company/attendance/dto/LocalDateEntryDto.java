package shamu.company.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocalDateEntryDto {
  private String timeLogId;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private LocalDate week;

  private Integer duration;
}
