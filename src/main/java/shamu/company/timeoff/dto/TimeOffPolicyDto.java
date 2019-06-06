package shamu.company.timeoff.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class TimeOffPolicyDto {

  @HashidsFormat
  private Long id;

  private String name;

  private Boolean isLimited;
}
