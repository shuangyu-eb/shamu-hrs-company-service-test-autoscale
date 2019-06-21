package shamu.company.benefit.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BenefitPlanClusterDto {
  private String benefitPlanType;

  private Integer benefitPlanNumber = 0;

  private List<BenefitPlanPreviewDto> benefitPlans;

  public BenefitPlanClusterDto(String benefitPlanType, List<BenefitPlanPreviewDto> benefitPlans) {
    this.benefitPlanType = benefitPlanType;
    this.benefitPlans = benefitPlans;
  }
}
