package shamu.company.timeoff.dto;

import lombok.Data;

@Data
public class NewPolicyCheckFrontedDto {

  private String timeOffPolicyId;

  private String timeOffAccrualFrequencyId;

  private Boolean hasMilestone;
}
