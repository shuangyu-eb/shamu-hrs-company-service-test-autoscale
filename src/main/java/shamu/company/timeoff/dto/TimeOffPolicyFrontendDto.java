package shamu.company.timeoff.dto;

import lombok.Data;

@Data
public class TimeOffPolicyFrontendDto {

  private Integer accrualHours;

  private Boolean isLimited;

  private String policyName;

  private Integer startDate;

  private String timeOffAccrualFrequency;
}
