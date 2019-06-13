package shamu.company.benefit.pojo;

import lombok.Data;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.benefit.entity.BenefitPlanCoverage;

@Data
public class BenefitPlanCoveragePojo {

  private String coverageName;

  private Integer employeeCost;

  private Integer employerCost;

  public BenefitPlanCoverage getBenefitPlanCoverage(BenefitPlan benefitPlan) {
    return new BenefitPlanCoverage(this.coverageName, benefitPlan, this.employeeCost,
        this.employerCost);
  }
}
