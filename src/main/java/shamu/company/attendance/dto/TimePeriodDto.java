package shamu.company.attendance.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class TimePeriodDto {
  private String id;
  private Timestamp startDate;
  private Timestamp endDate;
}
