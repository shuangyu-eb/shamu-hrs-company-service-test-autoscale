package shamu.company.attendance.dto;

import lombok.Data;

@Data
public class OvertimePolicyOverviewDto {
  private String id;

  private String policyName;

  private Integer defaultPolicy;

  private Integer numberOfEmployees;
}
