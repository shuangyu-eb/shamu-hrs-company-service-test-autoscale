package shamu.company.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalDateEntryDto {
  private String timeLogId;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private LocalDate week;

  private Integer duration;
}
