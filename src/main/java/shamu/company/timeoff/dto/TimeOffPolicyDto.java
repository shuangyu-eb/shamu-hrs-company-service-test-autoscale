package shamu.company.timeoff.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import shamu.company.hashids.HashidsFormat;

@Data
@NoArgsConstructor
public class TimeOffPolicyDto {

  @HashidsFormat
  private Long id;

  private String name;

  private Boolean isLimited;
}
