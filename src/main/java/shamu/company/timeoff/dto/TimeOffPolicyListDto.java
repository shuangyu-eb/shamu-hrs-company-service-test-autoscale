package shamu.company.timeoff.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class TimeOffPolicyListDto {

  @HashidsFormat
  private Long id;

  private String name;

  private Integer employee;

  private Boolean isLimited;
}
