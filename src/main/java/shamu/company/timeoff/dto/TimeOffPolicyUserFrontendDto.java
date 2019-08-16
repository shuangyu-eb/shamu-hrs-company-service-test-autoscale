package shamu.company.timeoff.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class TimeOffPolicyUserFrontendDto {

  @HashidsFormat
  private Long id;

  @HashidsFormat
  private Long userId;

  @HashidsFormat
  private Long timeOffPolicyId;

  private Integer balance = 0;
}
