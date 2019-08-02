package shamu.company.timeoff.dto;

import java.sql.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TimeOffRequestDateDto {

  private Timestamp date;

  private Integer hours;

  public TimeOffRequestDateDto(String date, String hours) {
    setDate(Timestamp.valueOf(date));
    setHours(Integer.valueOf(hours));
  }
}
