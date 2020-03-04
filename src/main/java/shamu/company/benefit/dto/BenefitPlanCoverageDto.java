package shamu.company.benefit.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class BenefitPlanCoverageDto {

  private String id;

  private String coverageName;

  private BigDecimal employeeCost;

  private BigDecimal employerCost;

  private String coverageId;
}
