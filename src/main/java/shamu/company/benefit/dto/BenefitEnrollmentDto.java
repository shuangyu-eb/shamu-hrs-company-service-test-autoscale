package shamu.company.benefit.dto;

import java.util.List;
import lombok.Data;

@Data
public class BenefitEnrollmentDto {

  private String id;

  private String benefitPlanId;

  private String title;

  private List<BenefitPlanCoverageDto> coverages;

  private String type;

  private String coverageType;

  private Integer cost;

  private Integer employerCost;

  private List<BenefitDependentDto> dependents;
}
