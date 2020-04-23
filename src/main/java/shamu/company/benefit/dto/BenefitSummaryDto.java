package shamu.company.benefit.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import shamu.company.benefit.entity.BenefitPlanDependent;

@Data
public class BenefitSummaryDto {
  private Long benefitNumber;

  private BigDecimal benefitCost;

  private Long dependentNumber;

  private List<BenefitPlanDependent> dependentUsers;
}
