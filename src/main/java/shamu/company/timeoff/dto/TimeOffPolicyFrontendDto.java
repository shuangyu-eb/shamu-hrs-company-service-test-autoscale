package shamu.company.timeoff.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;



@Data
public class TimeOffPolicyFrontendDto {

  private Integer accrualHours;

  private Boolean isLimited;

  @NotBlank
  private String policyName;

  private Integer startDate;

  private String timeOffAccrualFrequency;
}
