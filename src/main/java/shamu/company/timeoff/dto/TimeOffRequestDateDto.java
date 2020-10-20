package shamu.company.timeoff.dto;

import java.sql.Timestamp;
import lombok.Data;

@Data
public class TimeOffRequestDateDto {

  private String id;

  private Timestamp date;

  private Integer hours;
}
