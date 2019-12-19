package shamu.company.benefit.dto;

import lombok.Data;

@Data
public class BenefitPlanCoverageDto {

  private String id;

  private String coverageName;

  private Integer employeeCost;

  private Integer employerCost;
}
