package shamu.company.benefit.dto;

import lombok.Data;
import shamu.company.benefit.entity.BenefitPlan;
import shamu.company.hashids.HashidsFormat;

@Data
public class BenefitPlanDto {
  @HashidsFormat
  private Long id;

  public BenefitPlanDto(BenefitPlan benefitPlan) {
    this.id = benefitPlan.getId();
  }
}
