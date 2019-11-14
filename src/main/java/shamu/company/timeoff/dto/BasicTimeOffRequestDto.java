package shamu.company.timeoff.dto;

import java.sql.Timestamp;
import java.util.Set;
import lombok.Data;

@Data
public class BasicTimeOffRequestDto {

  private String id;

  private String userId;

  private String name;

  private Timestamp startDay;

  private Timestamp endDay;

  private Set<TimeOffRequestDateDto> dates;
}
