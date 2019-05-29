package shamu.company.timeoff.pojo;

import java.sql.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TimeOffRequestDatePojo {

  private Timestamp date;

  private Integer hours;

  public TimeOffRequestDatePojo(String date, String hours) {
    setDate(Timestamp.valueOf(date));
    setHours(Integer.valueOf(hours));
  }
}
