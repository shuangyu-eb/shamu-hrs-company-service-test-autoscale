package shamu.company.timeoff.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class TimeOffPolicyUserDto {

  @HashidsFormat
  private Long id;

  private TimeOffPolicyDto policy;

  private Integer balance;
}
