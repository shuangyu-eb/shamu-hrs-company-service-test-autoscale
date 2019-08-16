package shamu.company.timeoff.dto;

import lombok.Data;
import shamu.company.hashids.HashidsFormat;

@Data
public class TimeOffPolicyFrontendDto {

  private Integer accrualHours;

  private Boolean isLimited;

  private String policyName;

  private Integer startDate;

  @HashidsFormat
  private Long timeOffAccrualFrequency;
}
