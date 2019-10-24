package shamu.company.timeoff.dto;

import java.sql.Timestamp;
import java.util.Set;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class BasicTimeOffRequestDto {

  @HashidsFormat
  private Long id;

  @HashidsFormat
  private Long userId;

  private String name;

  private Timestamp startDay;

  private Timestamp endDay;

  private Set<TimeOffRequestDateDto> dates;
}
