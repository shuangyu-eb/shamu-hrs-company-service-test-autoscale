package shamu.company.timeoff.dto;

import java.sql.Timestamp;
import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class BasicTimeOffRequestDto {

  @HashidsFormat
  private Long id;

  private String name;

  private Timestamp startDay;

  private Timestamp endDay;
}
